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

package io.novaordis.release;

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.clad.ReleaseApplicationRuntime;
import io.novaordis.release.model.ArtifactType;
import io.novaordis.release.model.MockProject;
import io.novaordis.release.model.MockProjectBuilder;
import io.novaordis.release.sequences.BuildSequence;
import io.novaordis.release.sequences.CompletionSequence;
import io.novaordis.release.sequences.ExecutionHistory;
import io.novaordis.release.sequences.InstallSequence;
import io.novaordis.release.sequences.PublishSequence;
import io.novaordis.release.sequences.QualificationSequence;
import io.novaordis.release.sequences.Sequence;
import io.novaordis.release.sequences.SequenceController;
import io.novaordis.release.sequences.SequenceExecutionContext;
import io.novaordis.release.sequences.SequenceOperation;
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.expressions.Scope;
import io.novaordis.utilities.os.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class ReleaseCommandTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ReleaseCommandTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File scratchDirectory;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void before() throws Exception {

        String projectBaseDirName = System.getProperty("basedir");
        scratchDirectory = new File(projectBaseDirName, "target/test-scratch");
        assertTrue(scratchDirectory.isDirectory());

        System.setProperty("os.class", MockOS.class.getName());
    }

    @After
    public void after() throws Exception {

        ((MockOS)OS.getInstance()).reset();

        ReleaseMode.custom.reset();

        System.clearProperty("os.class");

        //
        // scratch directory cleanup
        //

        assertTrue(Files.rmdir(scratchDirectory, false));
    }

    // configure() -----------------------------------------------------------------------------------------------------

    @Test
    public void configure_info() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        List<String> args = new ArrayList<>(Arrays.asList("info", "something"));

        c.configure(0, args);

        assertEquals(ReleaseMode.info, c.getMode());

        assertEquals(1, args.size());
        assertEquals("something", args.get(0));
    }

    @Test
    public void configure_snapshot() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        List<String> args = new ArrayList<>(Arrays.asList("snapshot", "something"));

        c.configure(0, args);

        assertEquals(ReleaseMode.snapshot, c.getMode());

        assertEquals(1, args.size());
        assertEquals("something", args.get(0));
    }

    @Test
    public void configure_CustomReleaseString_ExplicitCustomQualifier_ValidReleaseLabel() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        //
        // the argument is interpreted as a literal custom version
        //

        List<String> args = new ArrayList<>(Arrays.asList("custom", "1.0.0", "something"));

        c.configure(0, args);

        ReleaseMode rm = c.getMode();
        assertEquals(ReleaseMode.custom, rm);
        assertEquals(new Version("1.0.0"), rm.getCustomVersion());

        assertEquals(1, args.size());
        assertEquals("something", args.get(0));
    }

    @Test
    public void configure_CustomReleaseString_ExplicitCustomQualifier_MissingReleaseLabel() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        //
        // the argument is interpreted as a literal custom version
        //

        List<String> args = new ArrayList<>(Collections.singletonList("custom"));

        try {

            c.configure(0, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("missing custom release version", msg);

        }
    }

    @Test
    public void configure_CustomReleaseString_ExplicitCustomQualifier_InvalidReleaseLabel() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        //
        // the argument is interpreted as a literal custom version
        //

        List<String> args = new ArrayList<>(Arrays.asList("custom", "something"));

        try {

            c.configure(0, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid custom release label \"something\": VersionFormatException invalid numeric version component \"something\"", msg);

        }
    }

    @Test
    public void configure_CustomRelease_FirstArgumentAnInvalidVersionLabel() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        //
        // the argument is interpreted as a literal custom version
        //

        List<String> args = new ArrayList<>(Arrays.asList("something", "else"));

        //
        // the first argument is interpreted as a custom (invalid) release label
        //

        try {

            c.configure(0, args);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid custom release label \"something\": VersionFormatException invalid numeric version component \"something\"", msg);

            VersionFormatException e2 = (VersionFormatException)e.getCause();
            String msg2 = e2.getMessage();
            log.info(msg2);
        }
    }

    @Test
    public void configure_InvalidReleaseModeOrCustomReleaseLabel() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        List<String> args = new ArrayList<>(Collections.singletonList("something"));

        try {

            c.configure(0, args);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid custom release label \"something\": VersionFormatException invalid numeric version component \"something\"", msg);
        }
    }

    @Test
    public void configure_FirstArgumentVersionLabel() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        //
        // the first argument is interpreted as a literal custom version
        //

        List<String> args = new ArrayList<>(Arrays.asList("1.2.3", "else"));

        c.configure(0, args);

        ReleaseMode mode = c.getMode();
        assertEquals(ReleaseMode.custom, mode);
        assertEquals(new Version("1.2.3"), mode.getCustomVersion());

        assertEquals(1, args.size());
        assertEquals("else", args.get(0));
    }

    // configure()/options ---------------------------------------------------------------------------------------------

    @Test
    public void configure_NoTests() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        List<String> args = new ArrayList<>(Arrays.asList("snapshot", "--no-tests", "something"));

        c.configure(0, args);

        assertTrue(c.isNoTests());
        assertFalse(c.isNoPush());
        assertFalse(c.isNoInstall());

        assertEquals(1, args.size());
        assertEquals("something", args.get(0));
    }

    @Test
    public void configure_NoPush() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        List<String> args = new ArrayList<>(Arrays.asList("snapshot", "--no-push", "something"));

        c.configure(0, args);

        assertFalse(c.isNoTests());
        assertTrue(c.isNoPush());
        assertFalse(c.isNoInstall());

        assertEquals(1, args.size());
        assertEquals("something", args.get(0));
    }

    @Test
    public void configure_NoInstall() throws Exception {

        ReleaseCommand c = new ReleaseCommand();

        List<String> args = new ArrayList<>(Arrays.asList("snapshot", "--no-install", "something"));

        c.configure(0, args);

        assertFalse(c.isNoTests());
        assertFalse(c.isNoPush());
        assertTrue(c.isNoInstall());

        assertEquals(1, args.size());
        assertEquals("something", args.get(0));
    }

    // info command ----------------------------------------------------------------------------------------------------

    @Test
    public void info() throws Exception {

        File crtDir = new File(scratchDirectory, "test");
        assertTrue(Files.mkdir(crtDir));
        assertTrue(crtDir.isDirectory());
        assertFalse(new File(crtDir, "pom.xml").isFile());

        ReleaseCommand command = new ReleaseCommand();
        command.setMode(ReleaseMode.info);

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.init(mc);
        mr.setCurrentDirectory(crtDir);

        try {
            command.execute(mr);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            log.info(e.getMessage());
            assertEquals("pom.xml not found in the current directory", e.getMessage());
        }
    }

    // release sequence ------------------------------------------------------------------------------------------------

    @Test
    public void types_WithInstall() throws Exception {

        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes(false);
        assertEquals(5, types.size());
        assertEquals(QualificationSequence.class, types.get(0));
        assertEquals(BuildSequence.class, types.get(1));
        assertEquals(PublishSequence.class, types.get(2));
        assertEquals(InstallSequence.class, types.get(3));
        assertEquals(CompletionSequence.class, types.get(4));
    }

    @Test
    public void types_WithoutInstall() throws Exception {

        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes(true);
        assertEquals(4, types.size());
        assertEquals(QualificationSequence.class, types.get(0));
        assertEquals(BuildSequence.class, types.get(1));
        assertEquals(PublishSequence.class, types.get(2));
        assertEquals(CompletionSequence.class, types.get(3));
    }

    /**
     * This test is aimed at insuring that a "release" command configuration propagates successfully to the internal
     * machinery - when there are no special configurations.
     */
    @Test
    public void execute_ExecutionOptionsTransfer_NoOptions() throws Exception {

        MockProject mp = new MockProject("1");
        MockProjectBuilder mpb = new MockProjectBuilder(mp);
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        Scope s = mr.getRootScope();

        ReleaseCommand c = new ReleaseCommand();

        c.setProjectBuilder(mpb);

        assertFalse(c.isNoTests());
        assertFalse(c.isNoPush());
        assertFalse(c.isNoInstall());

        try {

            c.execute(mr);
            fail("should throw exception");
        }
        catch(Exception e) {

            //
            // that is fine, the state is not appropriately set to execute all sequences, but we're only interested
            // in whether the state of the variable provider was changed
            //

            String msg = e.getMessage();
            log.info(msg);
        }

        assertFalse((Boolean) s.getVariable(ConfigurationLabels.QUALIFICATION_NO_TESTS).get());
        assertFalse((Boolean) s.getVariable(ConfigurationLabels.PUBLISH_NO_PUSH).get());
        assertFalse((Boolean) s.getVariable(ConfigurationLabels.INSTALL_NO_INSTALL).get());

        //
        // also make sure that all sequences are about to be executed
        //

        SequenceController sc = c.getSequenceController();
        List<Sequence> sequences = sc.getSequences();
        assertEquals(5, sequences.size());
        assertTrue(sequences.get(0) instanceof QualificationSequence);
        assertTrue(sequences.get(1) instanceof BuildSequence);
        assertTrue(sequences.get(2) instanceof PublishSequence);
        assertTrue(sequences.get(3) instanceof InstallSequence);
        assertTrue(sequences.get(4) instanceof CompletionSequence);
    }

    /**
     * This test is aimed at insuring that a "release" command configuration propagates successfully to the internal
     * machinery - when there are special configurations.
     */
    @Test
    public void execute_ExecutionOptionsTransfer_ThereAreOptions() throws Exception {

        MockProject mp = new MockProject("1");
        MockProjectBuilder mpb = new MockProjectBuilder(mp);
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        Scope s = mr.getRootScope();

        ReleaseCommand c = new ReleaseCommand();

        c.setProjectBuilder(mpb);

        c.setNoTests(true);
        c.setNoPush(true);
        c.setNoInstall(true);

        try {

            c.execute(mr);
            fail("should throw exception");
        }
        catch(Exception e) {

            //
            // that is fine, the state is not appropriately set to execute all sequences, but we're only interested
            // in whether the state of the variable provider was changed
            //

            String msg = e.getMessage();
            log.info(msg);
        }

        assertTrue((Boolean)s.getVariable(ConfigurationLabels.QUALIFICATION_NO_TESTS).get());
        assertTrue((Boolean) s.getVariable(ConfigurationLabels.PUBLISH_NO_PUSH).get());
        assertTrue((Boolean) s.getVariable(ConfigurationLabels.INSTALL_NO_INSTALL).get());

        //
        // also make sure that all the install sequence is missing from the execution list
        //

        SequenceController sc = c.getSequenceController();
        List<Sequence> sequences = sc.getSequences();
        assertEquals(4, sequences.size());
        assertTrue(sequences.get(0) instanceof QualificationSequence);
        assertTrue(sequences.get(1) instanceof BuildSequence);
        assertTrue(sequences.get(2) instanceof PublishSequence);
        assertTrue(sequences.get(3) instanceof CompletionSequence);
    }

    @Test
    public void execute_FirstSequenceFails_AllUndosMustExecute() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockProjectBuilder mb = new MockProjectBuilder(new MockProject("1-SNAPSHOT-1"));
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        mr.init(mc);

        ReleaseCommand c = new ReleaseCommand();
        c.setMode(ReleaseMode.snapshot);
        c.setProjectBuilder(mb);

        //
        // the qualification sequence fails on synthetic reasons - we break the configuration to fail on get()
        //

        mc.breakGet();

        try {

            c.execute(mr);
            fail("should have thrown exception");
        }
        catch(RuntimeException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("SYNTHETIC", msg);
        }

        //
        // make sure the failed execution shows up in history and then undo is invoked on all sequences
        //

        SequenceExecutionContext ctx = mr.getLastExecutionContext();
        ExecutionHistory history = ctx.getHistory();

        //
        // make sure that undo was called on all sequences
        //
        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes();
        assertEquals(types.size() + 1, history.length());

        //
        // the first in history is the failed sequence
        //

        SequenceOperation so = history.getOperation(0);

        assertEquals("execute", so.getMethodName());
        assertEquals(QualificationSequence.class, so.getTarget().getClass());
        assertFalse(so.wasSuccess());
        assertFalse(so.didChangeState());

        //
        // the rest are undos, make sure that undo was called in inverse order
        //
        for(int i = 0; i < types.size(); i ++) {

            so = history.getOperation(history.length() - i - 1);
            assertEquals("undo", so.getMethodName());
            assertTrue(types.get(i).equals(so.getTarget().getClass()));
            assertTrue(so.wasSuccess());
            assertFalse(so.didChangeState());
        }
    }

    @Test
    public void execute_middleSequenceFails_AllUndosMustExecute() throws Exception {

        MockProjectBuilder mb = new MockProjectBuilder(new MockProject("1-SNAPSHOT-1"));

        ReleaseCommand c = new ReleaseCommand();
        c.setMode(ReleaseMode.snapshot);
        c.setProjectBuilder(mb);

        ReleaseApplicationRuntime r = new ReleaseApplicationRuntime();
        Configuration conf = new MockConfiguration();
        r.init(conf);

        //
        // we're executing into a mock OS, tell it to "pass" the tests
        //

        ((MockOS)OS.getInstance()).addToCommandsThatSucceed(
                conf.get(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS));

        //
        // make the build sequence fail on synthetic reasons (remove a command it expects from configuration), as it is
        // in the middle
        //

        conf.set(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS, null);
        assertNull(conf.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS));

        try {

            c.execute(r);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to build without tests was not configured for this project", msg);
        }

        //
        // make sure the successful and the failed executions shows up in history and then undo is invoked on all
        // sequences
        //

        SequenceExecutionContext ctx = r.getLastExecutionContext();
        ExecutionHistory history = ctx.getHistory();

        //
        // the first in history is a successful qualification
        //

        SequenceOperation so = history.getOperation(0);

        assertEquals("execute", so.getMethodName());
        assertEquals(QualificationSequence.class, so.getTarget().getClass());
        assertTrue(so.wasSuccess());
        assertFalse(so.didChangeState());

        //
        // then we have the failed build
        //

        so = history.getOperation(1);

        assertEquals("execute", so.getMethodName());
        assertEquals(BuildSequence.class, so.getTarget().getClass());
        assertFalse(so.wasSuccess());
        assertFalse(so.didChangeState());


        //
        // make sure undo is invoked on all sequences
        //
        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes();
        assertEquals(types.size() + 2, history.length());

        //
        // the rest are undos, make sure that undo was called in inverse order
        //
        for(int i = 0; i < types.size(); i ++) {

            so = history.getOperation(history.length() - i - 1);
            assertEquals("undo", so.getMethodName());
            assertTrue(types.get(i).equals(so.getTarget().getClass()));
            assertTrue(so.wasSuccess());
            assertFalse(so.didChangeState());
        }
    }

    @Test
    public void execute_Success_Snapshot() throws Exception {

        MockReleaseApplicationRuntime r = new MockReleaseApplicationRuntime();
        r.setBinaryDistributionTopLevelDirectoryName("mock-artifact");
        MockConfiguration conf = new MockConfiguration();
        r.init(conf);

        ReleaseCommand c = new ReleaseCommand();

        MockProject mp = new MockProject("1-SNAPSHOT-1");

        //
        // simulate a binary installation - the artifact must be a real file
        //

        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File("mock-artifact.zip"), null);

        //
        // we need that artifact to "exist"
        //
        assertTrue(Files.write(new File(scratchDirectory, "mock-artifact.zip"), "MOCK ZIP FILE"));

        //
        // overwrite whatever is available in the mock configuration as
        // ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT with the scratch directory
        //
        conf.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, scratchDirectory.getAbsolutePath());

        //
        // create a "runtime directory" and the files we expect to find there
        //
        File runtimeDir = new File(scratchDirectory, "mock-runtime-dir");
        assertTrue(runtimeDir.mkdir());
        conf.set(ConfigurationLabels.INSTALLATION_DIRECTORY, runtimeDir.getAbsolutePath());
        File installationFile = new File(runtimeDir, "mock-artifact/bin/.install");
        assertTrue(Files.write(installationFile, "MOCK INSTALLATION FILE"));
        assertTrue(Files.chmod(installationFile, "r-xr--r--"));

        MockProjectBuilder mb = new MockProjectBuilder(mp);
        c.setProjectBuilder(mb);

        c.setMode(ReleaseMode.snapshot);

        //
        // we're executing into a mock OS, tell it execute successfully *all* commands
        //

        ((MockOS)OS.getInstance()).allCommandsSucceedByDefault();

        //
        // execution must be successful
        //

        c.execute(r);

        //
        // make sure all sequences have been executed in order and are successful
        //

        SequenceExecutionContext ctx = r.getLastExecutionContext();
        ExecutionHistory history = ctx.getHistory();

        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes();
        assertEquals(history.length(), types.size());

        for(int i = 0; i < history.length(); i++) {

            SequenceOperation so = history.getOperation(i);

            assertEquals("execute", so.getMethodName());

            Class<? extends Sequence> type = types.get(i);
            assertEquals(type, so.getTarget().getClass());
            assertTrue(so.wasSuccess());

            if (type.equals(QualificationSequence.class)) {
                assertFalse(so.didChangeState());
            }
            else if (type.equals(BuildSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(PublishSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(InstallSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(CompletionSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else {
                fail("unknown sequence type " + type);
            }
        }

        //
        // the "saved" version should be 1-SNAPSHOT-2
        //

        Version v = mp.getVersion();
        assertEquals(new Version("1-SNAPSHOT-2"), v);
        Version sv = mp.getLastSavedVersion();
        assertEquals(new Version("1-SNAPSHOT-2"), sv);
    }

    @Test
    public void execute_Success_Dot() throws Exception {

        MockReleaseApplicationRuntime r = new MockReleaseApplicationRuntime();
        r.setBinaryDistributionTopLevelDirectoryName("mock-artifact");

        MockConfiguration conf = new MockConfiguration();
        r.init(conf);

        ReleaseCommand c = new ReleaseCommand();

        MockProject mp = new MockProject("1-SNAPSHOT-1");

        //
        // simulate a binary installation - the artifact must be a real file
        //

        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File("mock-artifact.zip"), null);

        //
        // we need that artifact to "exist"
        //
        assertTrue(Files.write(new File(scratchDirectory, "mock-artifact.zip"), "MOCK ZIP FILE"));

        //
        // overwrite whatever is available in the mock configuration as
        // ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT with the scratch directory
        //
        conf.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, scratchDirectory.getAbsolutePath());

        //
        // create a "runtime directory" and the files we expect to find there
        //
        File runtimeDir = new File(scratchDirectory, "mock-runtime-dir");
        assertTrue(runtimeDir.mkdir());
        conf.set(ConfigurationLabels.INSTALLATION_DIRECTORY, runtimeDir.getAbsolutePath());
        File installationFile = new File(runtimeDir, "mock-artifact/bin/.install");
        assertTrue(Files.write(installationFile, "MOCK INSTALLATION FILE"));
        assertTrue(Files.chmod(installationFile, "r-xr--r--"));

        MockProjectBuilder mb = new MockProjectBuilder(mp);
        c.setProjectBuilder(mb);

        c.setMode(ReleaseMode.major);

        //
        // we're executing into a mock OS, tell it execute successfully *all* commands
        //

        ((MockOS)OS.getInstance()).allCommandsSucceedByDefault();

        //
        // execution must be successful
        //

        c.execute(r);

        //
        // make sure all sequences have been executed in order and are successful
        //

        SequenceExecutionContext ctx = r.getLastExecutionContext();
        ExecutionHistory history = ctx.getHistory();

        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes();
        assertEquals(history.length(), types.size());

        for(int i = 0; i < history.length(); i++) {

            SequenceOperation so = history.getOperation(i);

            assertEquals("execute", so.getMethodName());

            Class<? extends Sequence> type = types.get(i);
            assertEquals(type, so.getTarget().getClass());
            assertTrue(so.wasSuccess());

            if (type.equals(QualificationSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(BuildSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(PublishSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(InstallSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(CompletionSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else {
                fail("unknown sequence type " + type);
            }
        }

        //
        // the "saved" version should be 1-SNAPSHOT-2
        //

        Version v = mp.getVersion();
        assertEquals(new Version("1.0.1-SNAPSHOT-1"), v);
        Version sv = mp.getLastSavedVersion();
        assertEquals(new Version("1.0.1-SNAPSHOT-1"), sv);
    }

    @Test
    public void execute_Success_Custom() throws Exception {

        MockReleaseApplicationRuntime r = new MockReleaseApplicationRuntime();
        r.setBinaryDistributionTopLevelDirectoryName("mock-artifact");

        MockConfiguration conf = new MockConfiguration();
        r.init(conf);

        ReleaseCommand c = new ReleaseCommand();

        MockProject mp = new MockProject("1-SNAPSHOT-1");

        //
        // simulate a binary installation - the artifact must be a real file
        //

        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File("mock-artifact.zip"), null);

        //
        // we need that artifact to "exist"
        //
        assertTrue(Files.write(new File(scratchDirectory, "mock-artifact.zip"), "MOCK ZIP FILE"));

        //
        // overwrite whatever is available in the mock configuration as
        // ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT with the scratch directory
        //
        conf.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, scratchDirectory.getAbsolutePath());

        //
        // create a "runtime directory" and the files we expect to find there
        //
        File runtimeDir = new File(scratchDirectory, "mock-runtime-dir");
        assertTrue(runtimeDir.mkdir());
        conf.set(ConfigurationLabels.INSTALLATION_DIRECTORY, runtimeDir.getAbsolutePath());
        File installationFile = new File(runtimeDir, "mock-artifact/bin/.install");
        assertTrue(Files.write(installationFile, "MOCK INSTALLATION FILE"));
        assertTrue(Files.chmod(installationFile, "r-xr--r--"));

        MockProjectBuilder mb = new MockProjectBuilder(mp);
        c.setProjectBuilder(mb);

        ReleaseMode rm = ReleaseMode.custom;
        rm.setCustomLabel("1-SNAPSHOT-2");
        c.setMode(rm);


        //
        // we're executing into a mock OS, tell it execute successfully *all* commands
        //

        ((MockOS)OS.getInstance()).allCommandsSucceedByDefault();

        //
        // execution must be successful
        //

        c.execute(r);

        //
        // make sure all sequences have been executed in order and are successful
        //

        SequenceExecutionContext ctx = r.getLastExecutionContext();
        ExecutionHistory history = ctx.getHistory();

        List<Class<? extends Sequence>> types = ReleaseCommand.getSequenceTypes();
        assertEquals(history.length(), types.size());

        for(int i = 0; i < history.length(); i++) {

            SequenceOperation so = history.getOperation(i);

            assertEquals("execute", so.getMethodName());

            Class<? extends Sequence> type = types.get(i);
            assertEquals(type, so.getTarget().getClass());
            assertTrue(so.wasSuccess());

            if (type.equals(QualificationSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(BuildSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(PublishSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(InstallSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else if (type.equals(CompletionSequence.class)) {
                assertTrue(so.didChangeState());
            }
            else {
                fail("unknown sequence type " + type);
            }
        }

        //
        // the "saved" version should be 1-SNAPSHOT-2
        //

        Version v = mp.getVersion();
        assertEquals(new Version("1-SNAPSHOT-3"), v);
        Version sv = mp.getLastSavedVersion();
        assertEquals(new Version("1-SNAPSHOT-3"), sv);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
