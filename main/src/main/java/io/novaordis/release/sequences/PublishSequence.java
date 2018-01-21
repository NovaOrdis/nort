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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.OutputUtil;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.clad.ReleaseApplicationRuntime;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.expressions.Scope;
import io.novaordis.utilities.expressions.UndeclaredVariableException;
import io.novaordis.utilities.expressions.VariableReferenceResolver;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class PublishSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PublishSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Decide whether to push the release artifacts to a remote repository or not, depending on the release type.
     *
     * A snapshot release remains local, a dot release is pushed.
     */
    @SuppressWarnings("WeakerAccess")
    static boolean isPublishRemotely(Version version) {

        return !version.isSnapshot();
    }

    // Package Protected Static ----------------------------------------------------------------------------------------

    /**
     *  Install the artifacts into the local repository, and if the artifacts qualify, into the public repository.
     *  Fail if the local artifacts are not available.
     */
    @SuppressWarnings("WeakerAccess")
    static boolean publishArtifacts(ApplicationRuntime r, Configuration c, boolean noPush) throws Exception {

        Version currentVersion =
                new Version((String)r.getRootScope().getVariable(ConfigurationLabels.CURRENT_VERSION).get());

        String mavenCommand = "mvn jar:jar source:jar install:install";

        if (isPublishRemotely(currentVersion)) {

            if (noPush) {

                throw new UserErrorException(
                        "cannot make a dot release without pushing externally the binary artifacts");
            }

            mavenCommand += " deploy:deploy";
        }

        NativeExecutionResult er = OutputUtil.handleNativeCommandOutput(OS.getInstance().execute(mavenCommand), r, c);

        if (er.isFailure()) { throw new UserErrorException("publishing failed"); }

        r.info(currentVersion + " publishing ok");

        return true;
    }

    /**
     * Commits and tags code changes in the code repository and optionally pushes to the remote repository.
     *
     * @param noPush true means only apply changes to local repositories, don't attempt to push anything over the
     *               network
     */
    @SuppressWarnings("WeakerAccess")
    static boolean publishCodeChanges(ApplicationRuntime r, Configuration c, boolean noPush) throws Exception {

        log.debug("publishing code changes into local repository ...");

        boolean stateChanged = false;

        //noinspection ConstantConditions
        stateChanged |= addAndCommitIntoLocalCodeRepository(r, c);
        stateChanged |= tagLocalCodeRepository(r, c);

        if (noPush) {

            //
            // don't attempt to push anything externally
            //
            return stateChanged;
        }

        stateChanged |= pushToRemoteCodeRepository(r, c);
        return stateChanged;
    }

    static boolean addAndCommitIntoLocalCodeRepository(ApplicationRuntime r, Configuration c) throws Exception {

        log.debug("adding and committing to the local code repository ...");

        Version currentVersion =
                new Version((String)r.getRootScope().getVariable(ConfigurationLabels.CURRENT_VERSION).get());

        String addCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY);
        String commitCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY);

        if (addCommand == null) {

            throw new UserErrorException(
                    "the OS command to use to add to the local source repository was not configured for this project");
        }

        if (commitCommand == null) {

            throw new UserErrorException(
                    "the OS command to use to commit to the local source repository was not configured for this project");
        }

        NativeExecutionResult er = OutputUtil.handleNativeCommandOutput(OS.getInstance().execute(addCommand), r, c);

        if (er.isFailure()) {

            throw new UserErrorException("failed to add code to the local repository");
        }

        commitCommand = new VariableReferenceResolver().
                resolve(commitCommand, true, "current_version", currentVersion.getLiteral());

        er = OutputUtil.handleNativeCommandOutput(OS.getInstance().execute(commitCommand), r, c);

        if (er.isFailure()) {

            //
            // there may be nothing to commit, so we don't fail on commit failure, we just warn
            //
            r.warn("commit did not succeed, maybe because there was nothing to commit");
        }
        else {

            r.info(currentVersion + " source commit ok");
        }

        return true;
    }

    /**
     * We don't tag if we're a snapshot.
     */
    static boolean tagLocalCodeRepository(ApplicationRuntime r, Configuration c) throws Exception {

        Version currentVersion =
                new Version((String)r.getRootScope().getVariable(ConfigurationLabels.CURRENT_VERSION).get());

        if (currentVersion.isSnapshot()) {

            log.debug("local source repository will not be tagged because this is a snapshot versions");
            return false;
        }

        log.debug("tagging the local code repository ...");

        String tagCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY);

        if (tagCommand == null) {

            throw new UserErrorException(
                    "the OS command to use to tag the local source repository was not configured for this project");
        }

        String tag = computeTag(c, r.getRootScope());

        tagCommand = new VariableReferenceResolver().
                resolve(tagCommand, true, "current_version", currentVersion.toString(), "tag", tag);

        NativeExecutionResult er = OutputUtil.handleNativeCommandOutput(OS.getInstance().execute(tagCommand), r, c);

        if (er.isFailure()) {

            throw new UserErrorException("failed to tag the local source repository");
        }

        r.info(currentVersion + " source tag ok");

        return true;
    }

    static boolean pushToRemoteCodeRepository(ApplicationRuntime r, Configuration c) throws Exception {

        log.debug("pushing to the remote code repository ...");

        Version currentVersion =
                new Version((String)r.getRootScope().getVariable(ConfigurationLabels.CURRENT_VERSION).get());

        String pushCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY);

        if (pushCommand == null) {

            throw new UserErrorException(
                    "the OS command to use to push to the remote source repository was not configured for this project");
        }

        NativeExecutionResult er = OutputUtil.handleNativeCommandOutput(OS.getInstance().execute(pushCommand), r, c);

        if (er.isFailure()) {

            throw new UserErrorException("failed to push to remote source repository");
        }

        r.info(currentVersion + " source push ok");

        return true;
    }

    /**
     * TODO we should not need this method, the underlying logic should be built into Configuration
     */
    static String computeTag(Configuration c, Scope s) throws UserErrorException {

        //
        // attempt first to use an externally configured tag
        //

        String tag = c.get(ConfigurationLabels.RELEASE_TAG);

        if (tag == null) {

            tag = "release-${current.version}";
        }

        try {

            return s.evaluate(tag, true);
        }
        catch(UndeclaredVariableException e) {

            throw new UserErrorException("variable \"" + e.getUndeclaredVariableName() + "\" not defined");
        }
        catch(Exception e) {

            throw new UserErrorException(e);
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executeChangedState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public PublishSequence() {

        this.executeChangedState = false;
    }

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext c) throws Exception {

        boolean stateChanged = false;

        Configuration conf = c.getConfiguration();

        ReleaseApplicationRuntime r = c.getRuntime();

        boolean noPush = (Boolean)r.getRootScope().getVariable(ConfigurationLabels.PUBLISH_NO_PUSH).get();

        //noinspection ConstantConditions
        stateChanged |= publishArtifacts(r, conf, noPush);
        stateChanged |= publishCodeChanges(r, conf, noPush);

        this.executeChangedState = stateChanged;
        return stateChanged;
    }

    @Override
    public boolean undo(SequenceExecutionContext context) {

        if (!executeChangedState) {

            // noop
            return false;
        }

        throw new RuntimeException("undo() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean didExecuteChangeState() {

        return executeChangedState;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "publish sequence";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
