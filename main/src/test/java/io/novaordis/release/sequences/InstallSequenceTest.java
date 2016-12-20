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
import io.novaordis.release.model.MockProject;
import io.novaordis.release.model.ArtifactType;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class InstallSequenceTest extends SequenceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InstallSequenceTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File scratchDirectory;
    @SuppressWarnings("FieldCanBeLocal")
    private File baseDirectory;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void before() throws Exception {

        String projectBaseDirName = System.getProperty("basedir");
        scratchDirectory = new File(projectBaseDirName, "target/test-scratch");
        assertTrue(scratchDirectory.isDirectory());

        baseDirectory = new File(System.getProperty("basedir"));
        assertTrue(baseDirectory.isDirectory());

        System.setProperty("os.class", MockOS.class.getName());
    }

    @After
    public void after() throws Exception {

        ((MockOS) io.novaordis.utilities.os.OS.getInstance()).reset();
        System.clearProperty("os.class");

        assertTrue(Files.rmdir(scratchDirectory, false));
    }

    // execute() -------------------------------------------------------------------------------------------------------

    @Test
    public void execute_TheArtifactIsAJavaLibrary() throws Exception {

        InstallSequence is = new InstallSequence();

        MockProject mp = new MockProject("1.0");

        mp.addArtifact(ArtifactType.JAR_LIBRARY, new File("mock.jar"), null);

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        assertFalse(is.execute(c));
    }

    @Test
    public void execute_ArtifactRepositoryRootNotConfigured() throws Exception {

        InstallSequence is = new InstallSequence();

        MockProject mp = new MockProject("1.0");

        SequenceExecutionContext c = new SequenceExecutionContext(null, mp, null, false, null);

        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File("/does/not/matter.zip"), null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the local artifact repository root not configured", msg);
        }
    }

    @Test
    public void execute_ArtifactRepositoryRootNotAValidDirectory() throws Exception {

        InstallSequence is = new InstallSequence();

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, "/I/am/sure/there/is/no/such/directory");
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        MockProject mp = new MockProject("1.0");
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File("/does/not/matter.zip"), null);

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("the local artifact repository root .* is not a valid directory"));
        }
    }

    @Test
    public void execute_DistributionFileNotAvailableInTheArtifactRepositoryNorLocally() throws Exception {

        InstallSequence is = new InstallSequence();

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());

        MockProject mp = new MockProject("1.0");
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION,
                new File("I/am/sure/there/is/no/such/file/in/repository.zip"),
                new File("I/am/sure/there/is/no/such/file/locally.zip"));

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("binary distribution artifact not found in the artifact repository, nor in the local work area"));
        }
    }

    @Test
    public void execute_RuntimeDirectoryNotConfigured() throws Exception {

        InstallSequence is = new InstallSequence();

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, "test.zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the runtime directory not configured", msg);
        }
    }

    @Test
    public void execute_RuntimeDirectoryNotAValidDirectory() throws Exception {

        InstallSequence is = new InstallSequence();

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, "test.zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, "/I/am/sure/there/is/no/such/directory");

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("the runtime directory .* is not a valid directory"));
        }
    }

    @Test
    public void execute_RuntimeDirectoryNotWritable() throws Exception {

        InstallSequence is = new InstallSequence();

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, "test.zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        File rd = new File(scratchDirectory, "test-runtime-dir");
        assertTrue(rd.mkdir());
        assertTrue(Files.chmod(rd, "r--r--r--"));

        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, rd.getAbsolutePath());

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("the runtime directory .* is not writable"));
        }
    }

    @Test
    public void execute_ArtifactExtractionInRuntimeDirectoryFails() throws Exception {

        InstallSequence is = new InstallSequence();

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, "test.zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        File rd = new File(scratchDirectory, "test-runtime-dir");
        assertTrue(rd.mkdir());
        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, rd.getAbsolutePath());

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        MockOS mockOS = (MockOS) OS.getInstance();
        mockOS.allCommandsFail();

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("failed to extract.*"));
        }
    }

    @Test
    public void execute_NoEmbeddedInstallationScript() throws Exception {

        InstallSequence is = new InstallSequence();

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, "test.zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        File rd = new File(scratchDirectory, "test-runtime-dir");
        assertTrue(rd.mkdir());
        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, rd.getAbsolutePath());

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        MockOS mockOS = (MockOS) OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("no installation script .* found or the file is not executable"));
        }
    }

    @Test
    public void execute_EmbeddedInstallationScriptFails() throws Exception {

        InstallSequence is = new InstallSequence();

        String distributionFileName = "test-distribution-1.0";

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, distributionFileName + ".zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        File rd = new File(scratchDirectory, "test-runtime-dir");
        assertTrue(rd.mkdir());
        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, rd.getAbsolutePath());

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        mr.setBinaryDistributionTopLevelDirectoryName(distributionFileName);

        MockOS mockOS = (MockOS) OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        //
        // the only command that fails is the installation script
        //
        mockOS.addToCommandsThatFail(".install");

        //
        // make sure the script is "found" as unzip is jut mock
        //

        File installationScript = new File(rd, distributionFileName + "/bin/.install");
        assertTrue(Files.write(installationScript, "#/bin/bash\n\necho ."));
        assertTrue(Files.chmod(installationScript, "r-xr--r--"));

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        try {

            is.execute(c);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("installation failed", msg);
        }
    }

    @Test
    public void execute_Success_RepositoryFile() throws Exception {

        InstallSequence is = new InstallSequence();

        String distributionFileName = "test-distribution-1.0";

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();
        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());

        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());
        MockProject mp = new MockProject("1.0");
        File distributionFile = new File(localArtifactRepositoryRoot, distributionFileName + ".zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File(distributionFile.getName()), null);

        File rd = new File(scratchDirectory, "test-runtime-dir");
        assertTrue(rd.mkdir());
        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, rd.getAbsolutePath());

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        mr.setBinaryDistributionTopLevelDirectoryName(distributionFileName);

        MockOS mockOS = (MockOS) OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        //
        // successful execution
        //
        mockOS.allCommandsSucceedByDefault();

        //
        // make sure the script is "found" as unzip is jut mock
        //

        File installationScript = new File(rd, distributionFileName + "/bin/.install");
        assertTrue(Files.write(installationScript, "#/bin/bash\n\necho ."));
        assertTrue(Files.chmod(installationScript, "r-xr--r--"));

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        assertTrue(is.execute(c));

        List<String> commands = mockOS.getHistory();
        assertEquals(2, commands.size());

        String command = commands.get(0);
        assertTrue(command.startsWith("unzip"));

        String command2 = commands.get(1);
        assertEquals(".install", command2);
    }

    @Test
    public void execute_Success_LocalFile() throws Exception {

        InstallSequence is = new InstallSequence();

        String distributionFileName = "test-distribution-1.0";

        //
        // the local artifact repository root must be configured and exist
        //
        MockConfiguration mc = new MockConfiguration();

        File localArtifactRepositoryRoot = new File(scratchDirectory, "mock-artifact-repository");
        assertTrue(localArtifactRepositoryRoot.mkdir());
        mc.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, localArtifactRepositoryRoot.getAbsolutePath());

        File targetDirSimulation = new File(scratchDirectory, "project/release/target");
        assertTrue(targetDirSimulation.mkdirs());

        MockProject mp = new MockProject("1.0");

        File distributionFile = new File(targetDirSimulation, distributionFileName + ".zip");
        assertTrue(Files.write(distributionFile, "..."));
        mp.addArtifact(ArtifactType.BINARY_DISTRIBUTION, new File("does-not-matter.zip"), distributionFile);

        File rd = new File(scratchDirectory, "test-runtime-dir");
        assertTrue(rd.mkdir());
        mc.set(ConfigurationLabels.INSTALLATION_DIRECTORY, rd.getAbsolutePath());

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        mr.setBinaryDistributionTopLevelDirectoryName(distributionFileName);

        MockOS mockOS = (MockOS) OS.getInstance();
        mockOS.allCommandsSucceedByDefault();

        //
        // successful execution
        //
        mockOS.allCommandsSucceedByDefault();

        //
        // make sure the script is "found" as unzip is jut mock
        //

        File installationScript = new File(rd, distributionFileName + "/bin/.install");
        assertTrue(Files.write(installationScript, "#/bin/bash\n\necho ."));
        assertTrue(Files.chmod(installationScript, "r-xr--r--"));

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, false, null);

        assertTrue(is.execute(c));

        List<String> commands = mockOS.getHistory();
        assertEquals(2, commands.size());

        String command = commands.get(0);
        assertTrue(command.startsWith("unzip"));

        String command2 = commands.get(1);
        assertEquals(".install", command2);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected InstallSequence getSequenceToTest() throws Exception {

        return new InstallSequence();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
