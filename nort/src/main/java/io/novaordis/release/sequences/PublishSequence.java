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
import io.novaordis.release.OutputUtil;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import io.novaordis.utilities.variable.StringWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class PublishSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PublishSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Package Protected Static ----------------------------------------------------------------------------------------

    /**
     *  Install the artifacts into the local repository, fail if the local artifacts are not available
     */
    static boolean publishArtifacts(ApplicationRuntime r, Configuration c, Version currentVersion) throws Exception {

        String localPublishingCommand = c.get(
                ConfigurationLabels.OS_COMMAND_TO_PUBLISH_INTO_LOCAL_REPOSITORY);

        if (localPublishingCommand == null) {
            throw new UserErrorException(
                    "the OS command to use to publish project artifacts into the local repository was not configured for this project");
        }

        log.debug("publishing artifacts into local repository with \"" + localPublishingCommand + "\" ...");

        NativeExecutionResult er = OutputUtil.
                handleNativeCommandOutput(OS.getInstance().execute(localPublishingCommand), r, c);

        if (er.isFailure()) { throw new UserErrorException("local publishing failed"); }

        r.info(currentVersion + " local publishing ok");

        return true;
    }

    /**
     * Commits and tags code changes in the code repository and optionally pushes to the remote repository.
     *
     * @param noPush true means only apply changes to local repositories, don't attempt to push anything over the
     *               network
     */
    static boolean publishCodeChanges(ApplicationRuntime r, Configuration c, Version currentVersion, boolean noPush)
            throws Exception {

        log.debug("publishing code changes into local repository ...");

        boolean stateChanged = false;

        //noinspection ConstantConditions
        stateChanged |= addAndCommitIntoLocalCodeRepository(r, c, currentVersion);
        stateChanged |= tagLocalCodeRepository(r, c, currentVersion);

        if (noPush) {

            //
            // don't attempt to push anything externally
            //
            return stateChanged;
        }

        stateChanged |= pushToRemoteCodeRepository(r, c, currentVersion);
        return stateChanged;
    }

    static boolean addAndCommitIntoLocalCodeRepository(ApplicationRuntime r, Configuration c, Version currentVersion)
            throws Exception {

        log.debug("adding and committing to the local code repository ...");

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

        commitCommand = new StringWithVariables(commitCommand).resolve("current_version", currentVersion.getLiteral());

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
    static boolean tagLocalCodeRepository(ApplicationRuntime r, Configuration c, Version currentVersion)
            throws Exception {

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

        //
        // resolve the current version and the tag
        //

        tagCommand = new StringWithVariables(tagCommand).resolve(
                "current_version", currentVersion.toString(), "tag", "release-" + currentVersion);

        NativeExecutionResult er = OutputUtil.handleNativeCommandOutput(OS.getInstance().execute(tagCommand), r, c);

        if (er.isFailure()) {
            throw new UserErrorException("failed to tag the local source repository");
        }

        r.info(currentVersion + " source tag ok");

        return true;
    }

    static boolean pushToRemoteCodeRepository(ApplicationRuntime r, Configuration c, Version currentVersion)
            throws Exception {

        log.debug("pushing to the remote code repository ...");

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
        ApplicationRuntime r = c.getRuntime();
        Version currentVersion = c.getCurrentVersion();
        boolean noPush = c.isNoPush();

        //noinspection ConstantConditions
        stateChanged |= publishArtifacts(r, conf, currentVersion);
        stateChanged |= publishCodeChanges(r, conf, currentVersion, noPush);

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

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
