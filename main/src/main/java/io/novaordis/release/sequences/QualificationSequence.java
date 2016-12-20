/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.release.sequences;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.ReleaseMode;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.model.Project;
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionUtil;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class QualificationSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(QualificationSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executeChangedState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public QualificationSequence() {

        this.executeChangedState = false;
    }

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext context) throws Exception {

        log.debug("executing the qualification sequence ...");

        insureCurrentVersionIsSnapshot(context);
        insureCustomReleaseHasCustomVersionLabel(context);
        incrementCurrentVersionIfNecessary(context);
        failIfInstalledVersionIsEqualOrNewer(context);

        boolean testsPassed = executeTests(context);

        if (testsPassed) {

            context.getRuntime().info(context.getCurrentVersion() + " tests ok");
        }
        else {

            throw new UserErrorException("tests failed");
        }

        return executeChangedState;
    }

    @Override
    public boolean undo(SequenceExecutionContext context) {

        //
        // there is nothing to undo, noop
        //

        return false;
    }

    @Override
    public boolean didExecuteChangeState() {

        return executeChangedState;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "qualification sequence";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void insureCurrentVersionIsSnapshot(SequenceExecutionContext context) throws Exception {

        log.debug("insuring the current version is a snapshot version ...");

        Version v = context.getCurrentVersion();

        log.debug("current version " + v);

        if (!v.isSnapshot()) {
            throw new UserErrorException(
                    "the current version (" + v + ") is not a snapshot version, cannot start the release sequence");
        }
    }

    public void insureCustomReleaseHasCustomVersionLabel(SequenceExecutionContext context) throws Exception {

        ReleaseMode rm = context.getReleaseMode();

        if (!rm.isCustom()) {

            return;
        }

        Version v = rm.getCustomVersion();

        if (v == null) {

            throw new IllegalArgumentException("custom release mode is missing the custom version information");
        }
    }

    /**
     * If the release is a dot release, or it is a custom release and the release string is different than the current
     * one, we increment/update version appropriately and we update it on the file system metadata, so tests can be run
     * with the correct version.
     *
     * A noop if the release is a snapshot release, as the state is already appropriate.
     *
     * The method will also update the context with the current version (which may be incremented).
     *
     * @exception UserErrorException if we attempt to update to a custom version that is older then the current.
     */
    void incrementCurrentVersionIfNecessary(SequenceExecutionContext context) throws Exception {

        ReleaseMode rm = context.getReleaseMode();

        log.debug("attempt to increment the current version, if necessary, release mode " + rm);


        Version currentVersion = context.getCurrentVersion();
        Version nextVersion = null;

        if (rm.isDot()) {

            //
            // if we're in a dot release mode, increment the release metadata before anything else, so we can run
            // relevant tests
            //

            //
            // this will fail if we're a custom dot patch that is older than the current
            //

            nextVersion = Version.nextVersion(currentVersion, rm);
        }
        else if (ReleaseMode.custom.equals(rm)) {

            nextVersion = rm.getCustomVersion();
        }

        if (nextVersion == null || nextVersion.equals(currentVersion)) {

            //
            // no version change, noop
            //
            return;
        }

        //
        // different versions
        //

        if (nextVersion.compareTo(currentVersion) < 0) {

            //
            // fail if we devolve
            //

            throw new UserErrorException(currentVersion + " cannot be changed to preceding " + nextVersion);
        }

        log.debug("updating current version to " + nextVersion);

        Project p = context.getProject();
        p.setVersion(nextVersion);

        //
        // we need the version change on disk, so the tests can be executed in top of the changed version
        //
        executeChangedState = p.save() || executeChangedState;

        //
        // version change, place the new version in the context
        //

        context.setCurrentVersion(nextVersion);
    }

    /**
     * Execute tests. The current version (possibly changed by the previous steps) is available in the context.
     *
     * @return true if all test executed successfully, false otherwise
     *
     * @exception UserErrorException on configuration errors
     * @exception Exception on native execution exceptions
     */
    boolean executeTests(SequenceExecutionContext context) throws Exception {

        boolean testsExecutedSuccessfully;

        Version currentVersion = context.getCurrentVersion();

        log.debug("running tests for release " + currentVersion + " ...");

        //
        // make sure we have the command required to run all tests
        //

        Configuration c = context.getConfiguration();

        String osCommandToExecuteAllTests = c.get(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS);

        if (osCommandToExecuteAllTests == null) {
            throw new UserErrorException(
                    "the OS command to use to execute all tests was not configured for this project");
        }

        log.debug("executing all tests with \"" + osCommandToExecuteAllTests + "\" ...");

        NativeExecutionResult executionResult = OS.getInstance().execute(osCommandToExecuteAllTests);

        //
        // the tests were executed, so let the subsequent sequences know; tests do not need to pass in order to set
        // this flag
        //

        context.setTestsExecuted(true);

        testsExecutedSuccessfully = executionResult.isSuccess();

        if (c.isVerbose()) {
            context.getRuntime().info(executionResult.getStdout());
        }

        return testsExecutedSuccessfully;
    }

    /**
     * This method encapsulates the logic that attempts to get the version that is already installed, and fails
     * if we're releasing same or older version.
     *
     * @throws UserErrorException if we established we're releasing the same or an older version
     */
    void failIfInstalledVersionIsEqualOrNewer(SequenceExecutionContext context) throws Exception {

        Configuration c = context.getConfiguration();
        ApplicationRuntime r = context.getRuntime();

        String command = c.get(ConfigurationLabels.OS_COMMAND_TO_GET_INSTALLED_VERSION);

        if (command == null) {

            //
            // we won't perform the test, but warn
            //

            r.warn("cannot get the version of currently installed " + context.getProject().getName() +
                    ": command to read it is not configured");
            return;
        }

        NativeExecutionResult executionResult = OS.getInstance().execute(command);

        if (executionResult.isFailure()) {

            //
            // we won't perform the test, but warn instead
            //

            r.warn(
                    "failed to execute the command that gets the version of the already installed release (" +
                            command + ")");
            r.warn(executionResult.getStdout());
            r.warn(executionResult.getStderr());
            return;
        }

        String stdoutContent = executionResult.getStdout();

        log.debug("'" + command + "' output: \n" + stdoutContent);

        Version installedVersion;

        try {

            installedVersion = VersionUtil.fromCommandStdout(stdoutContent);
        }
        catch(Exception e) {

            //
            // don't fail, just warn
            //

            r.warn("invalid '" + command + "' output:");
            r.warn(e.getMessage());
            return;
        }

        if (installedVersion == null) {

            return;
        }

        Version versionBeingReleased = context.getCurrentVersion();

        if (installedVersion.compareTo(versionBeingReleased) > 0) {

            throw new UserErrorException(
                    "installed version " + installedVersion + " is newer than the version being released ("  +
                            versionBeingReleased + ")");
        }
        else if (installedVersion.equals(versionBeingReleased)) {

            throw new UserErrorException(
                    "installed version is identical to the version being released ("  +
                            versionBeingReleased + ")");
        }

        //
        // we're good
        //

        log.debug("installed version " + installedVersion + " is older than the version being released (" +
                versionBeingReleased + ")");
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
