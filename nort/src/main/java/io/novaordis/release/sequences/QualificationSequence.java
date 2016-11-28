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

        Configuration c = context.getConfiguration();
        ApplicationRuntime r = context.getRuntime();
        Project p = context.getProject();

        ReleaseMode rm = context.getReleaseMode();
        log.debug("release mode: " + rm);

        //
        // make sure the current version is a snapshot
        //

        Version currentVersion = p.getVersion();

        log.debug("current version " + currentVersion);

        if (!currentVersion.isSnapshot()) {
            throw new UserErrorException(
                    "the current version (" + currentVersion + ") is not a snapshot version, cannot start the release sequence");
        }

        if (rm.isDot()) {

            //
            // if we're in a dot release mode, increment the release metadata before anything else, so we can run
            // relevant tests
            //

            Version nextVersion = Version.nextVersion(currentVersion, rm);
            p.setVersion(nextVersion);
            //
            // we need the version change on disk, so the tests can be executed in top of the changed version
            //
            executeChangedState = p.save() || executeChangedState;
            currentVersion = nextVersion;
        }

        //
        // set the current version for subsequent sequences
        //

        context.setCurrentVersion(currentVersion);

        log.debug("running tests for release " + currentVersion + " ...");

        //
        // make sure we have the command required to run all tests
        //

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

        if (!executionResult.isSuccess()) {

            throw new UserErrorException("tests failed");
        }

        if (c.isVerbose()) {
            r.info(executionResult.getStdout());
        }

        r.info(currentVersion + " tests ok");

        return executeChangedState;
    }

    @Override
    public boolean undo(SequenceExecutionContext context) {

        //
        // there is nothing to undo, noop
        //

        return false;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
