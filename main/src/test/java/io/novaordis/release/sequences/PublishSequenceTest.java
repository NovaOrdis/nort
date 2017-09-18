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

import io.novaordis.release.MockConfiguration;
import io.novaordis.release.MockOS;
import io.novaordis.release.MockReleaseApplicationRuntime;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.model.maven.MockMavenProject;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.expressions.Scope;
import io.novaordis.utilities.expressions.ScopeImpl;
import io.novaordis.utilities.os.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class PublishSequenceTest extends SequenceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(PublishSequenceTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void before() throws Exception {

        System.setProperty("os.class", MockOS.class.getName());
    }

    @After
    public void after() throws Exception {

        ((MockOS) io.novaordis.utilities.os.OS.getInstance()).reset();
        System.clearProperty("os.class");
    }

    @Test
    public void osCommandToPublishLocallyNotConfigured() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        MockMavenProject mp = new MockMavenProject();

        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUBLISH_INTO_LOCAL_REPOSITORY, null);

        PublishSequence s = new PublishSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, null);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals(
                    "the OS command to use to publish project artifacts into the local repository was not configured for this project",
                    msg);
        }
    }

    @Test
    public void publishingFailure() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        MockMavenProject mp = new MockMavenProject();

        //
        // instruct the mock OS instance to fail when publishing
        //
        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUBLISH_INTO_LOCAL_REPOSITORY, "mock local publishing");
        mockOS.addToCommandsThatFail("mock local publishing");

        PublishSequence s = new PublishSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, null);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("local publishing failed", msg);
        }

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(1, executedCommands.size());
        assertEquals("mock local publishing", executedCommands.get(0));
    }

    // addAndCommitIntoLocalCodeRepository() ---------------------------------------------------------------------------

    @Test
    public void addAndCommitIntoLocalCodeRepository_AddCommandNotConfigured() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "0");
        MockConfiguration mc = new MockConfiguration();

        try {

            PublishSequence.addAndCommitIntoLocalCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to add to the local source repository was not configured for this project", msg);
        }
    }

    @Test
    public void addAndCommitIntoLocalCodeRepository_AddCommandFails() throws Exception {

        String mockAddCommand = "mock add to repository";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "0");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY, mockAddCommand);
        mc.set(ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY, "something");

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.addToCommandsThatFail(mockAddCommand);

        try {

            PublishSequence.addAndCommitIntoLocalCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("failed to add code to the local repository", msg);
        }
    }

    @Test
    public void addAndCommitIntoLocalCodeRepository_CommitCommandNotConfigured() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "0");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY, "something");

        try {

            PublishSequence.addAndCommitIntoLocalCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to commit to the local source repository was not configured for this project", msg);
        }
    }

    @Test
    public void addAndCommitIntoLocalCodeRepository_CommitCommandFails() throws Exception   {

        String mockAddCommand = "mock add to repository";
        String mockCommitCommand = "mock-commit ${current_version}";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "77.77");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY, mockAddCommand);
        mc.set(ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY, mockCommitCommand);

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.addToCommandsThatSucceed(mockAddCommand);
        mockOS.addToCommandsThatFail("mock-commit 77.77");

        // we also test that runtime variables are resolved

        assertTrue(PublishSequence.addAndCommitIntoLocalCodeRepository(mr, mc));

        //
        // we should get a warning
        //

        String warning = mr.getWarningContent().trim();
        assertEquals("commit did not succeed, maybe because there was nothing to commit", warning);

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(2, executedCommands.size());
        assertEquals(mockAddCommand, executedCommands.get(0));
        assertEquals("mock-commit 77.77", executedCommands.get(1));
    }

    @Test
    public void addAndCommitIntoLocalCodeRepository_AddAndCommitSucceed() throws Exception {

        String mockAddCommand = "mock add to repository";
        String mockCommitCommand = "mock-commit ${current_version}";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "88.88");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY, mockAddCommand);
        mc.set(ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY, mockCommitCommand);

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        // we also test that runtime variables are resolved

        assertTrue(PublishSequence.addAndCommitIntoLocalCodeRepository(mr, mc));

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(2, executedCommands.size());
        assertEquals(mockAddCommand, executedCommands.get(0));
        assertEquals("mock-commit 88.88", executedCommands.get(1));
    }

    // tagLocalCodeRepository() ----------------------------------------------------------------------------------------

    @Test
    public void tagLocalCodeRepository_snapshot_noop() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "1.0.0-SNAPSHOT-1");

        //
        // we're a snapshot, we must not tag
        //

        assertFalse(PublishSequence.tagLocalCodeRepository(mr, mc));
    }

    @Test
    public void tagLocalCodeRepository_tagCommandNotConfigured() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "1.0.0");

        try {

            PublishSequence.tagLocalCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to tag the local source repository was not configured for this project", msg);
        }
    }

    @Test
    public void tagLocalCodeRepository_TagFails() throws Exception   {

        String mockTagCommand = "mock tag";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "1");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY, mockTagCommand);

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.addToCommandsThatFail(mockTagCommand);

        try {

            PublishSequence.tagLocalCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("failed to tag the local source repository", msg);
        }
    }

    @Test
    public void tagLocalCodeRepository_TagSucceeds() throws Exception {

        String mockTagCommand = "mock-tag ${current_version} ${tag}";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "7.7");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY, mockTagCommand);

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        assertTrue(PublishSequence.tagLocalCodeRepository(mr, mc));

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(1, executedCommands.size());
        String commandWithVariablesReplaced = "mock-tag 7.7 release-7.7";
        assertEquals(commandWithVariablesReplaced, executedCommands.get(0));
    }

    // computeTag() ----------------------------------------------------------------------------------------------------

    @Test
    public void computeTag_TemplateSet_VariableDefined() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        Scope s = new ScopeImpl();

        mc.set(ConfigurationLabels.RELEASE_TAG, "something-${somethingelse}");
        s.declare("somethingelse", "blue");

        String tag = PublishSequence.computeTag(mc, s);
        assertEquals("something-blue", tag);
    }

    @Test
    public void computeTag_TemplateSet_VariableNotDefined() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        Scope s = new ScopeImpl();

        mc.set(ConfigurationLabels.RELEASE_TAG, "something-${somethingelse}");

        try {

            PublishSequence.computeTag(mc, s);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("VariableNotDefinedException \"somethingelse\" not defined", msg);
        }
    }

    @Test
    public void computeTag_TemplateNotSet_DefaultTagValue() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        Scope s = new ScopeImpl();
        s.declare(ConfigurationLabels.CURRENT_VERSION, "1.2.3-SNAPSHOT-4");
        String tag = PublishSequence.computeTag(mc, s);
        assertEquals("release-1.2.3-SNAPSHOT-4", tag);
    }

    // pushToRemoteCodeRepository() ------------------------------------------------------------------------------------

    @Test
    public void pushToRemoteCodeRepository_pushCommandNotConfigured() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "0");
        MockConfiguration mc = new MockConfiguration();

        try {

            PublishSequence.pushToRemoteCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to push to the remote source repository was not configured for this project", msg);
        }
    }

    @Test
    public void pushToRemoteCodeRepository_PushFails() throws Exception   {

        String mockCommand = "mock push";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "0");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY, mockCommand);

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.addToCommandsThatFail(mockCommand);

        try {

            PublishSequence.pushToRemoteCodeRepository(mr, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("failed to push to remote source repository", msg);
        }
    }

    @Test
    public void pushToRemoteCodeRepository_PushSucceeds() throws Exception {

        String mockCommand = "mock push";

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.getRootScope().declare(ConfigurationLabels.CURRENT_VERSION, "0");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY, mockCommand);

        MockOS mockOS = (MockOS)OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        assertTrue(PublishSequence.pushToRemoteCodeRepository(mr, mc));

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(1, executedCommands.size());
        assertEquals(mockCommand, executedCommands.get(0));
    }

    // end-to-end publishing success -----------------------------------------------------------------------------------

    @Test
    public void endToEndPublishingSuccess_NoPush() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        MockMavenProject mp = new MockMavenProject();

        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUBLISH_INTO_LOCAL_REPOSITORY, "mock-publish-artifacts");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY, "mock-source-add");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY, "mock-source-commit ${current_version}");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY, "mock-source-tag ${current_version} ${tag}");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY, "mock-push");
        mockOS.allCommandsSucceedByDefault();

        PublishSequence s = new PublishSequence();

        //
        // NO push
        //

        mr.getRootScope().declare(ConfigurationLabels.PUBLISH_NO_PUSH, "true");

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, null);

        c.setCurrentVersion(new Version("99.9"));

        s.execute(c);

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(4, executedCommands.size());
        assertEquals("mock-publish-artifacts", executedCommands.get(0));
        assertEquals("mock-source-add", executedCommands.get(1));
        assertEquals("mock-source-commit 99.9", executedCommands.get(2));
        assertEquals("mock-source-tag 99.9 release-99.9", executedCommands.get(3));
    }

    @Test
    public void endToEndPublishingSuccess_RemotePush() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        MockMavenProject mp = new MockMavenProject();

        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUBLISH_INTO_LOCAL_REPOSITORY, "mock-publish-artifacts");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY, "mock-source-add");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY, "mock-source-commit ${current_version}");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY, "mock-source-tag ${current_version} ${tag}");
        mc.set(ConfigurationLabels.OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY, "mock-push");
        mockOS.allCommandsSucceedByDefault();

        PublishSequence s = new PublishSequence();

        mr.getRootScope().declare(ConfigurationLabels.PUBLISH_NO_PUSH, "false");

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, null);

        c.setCurrentVersion(new Version("99.9"));

        s.execute(c);

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(5, executedCommands.size());
        assertEquals("mock-publish-artifacts", executedCommands.get(0));
        assertEquals("mock-source-add", executedCommands.get(1));
        assertEquals("mock-source-commit 99.9", executedCommands.get(2));
        assertEquals("mock-source-tag 99.9 release-99.9", executedCommands.get(3));
        assertEquals("mock-push", executedCommands.get(4));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected PublishSequence getSequenceToTest() throws Exception {

        return new PublishSequence();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
