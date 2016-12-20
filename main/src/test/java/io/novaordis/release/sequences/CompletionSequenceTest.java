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
import io.novaordis.release.ReleaseMode;
import io.novaordis.release.model.MockProject;
import io.novaordis.release.model.maven.MavenProject;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.os.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class CompletionSequenceTest extends SequenceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CompletionSequenceTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File scratchDirectory;
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

        ((MockOS) OS.getInstance()).reset();
        System.clearProperty("os.class");

        assertTrue(Files.rmdir(scratchDirectory, false));
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void successfulExecution_SnapshotRelease_SnapshotVersionOnDisk() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        File testPom = new File(new File(scratchDirectory, "test"), "test-pom.xml");
        assertTrue(Files.mkdir(testPom.getParentFile()));
        assertTrue(Files.cp(new File(baseDirectory, "src/test/resources/data/maven/pom-sample-snapshot.xml"), testPom));

        MavenProject mp = new MavenProject(testPom);

        CompletionSequence s = new CompletionSequence();

        //
        // snapshot release, the version on disk is snapshot
        //
        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, ReleaseMode.snapshot, false, null);

        assertTrue(mp.getVersion().isSnapshot());
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), mp.getVersion());

        //
        //
        //

        boolean stateChanged = s.execute(c);

        assertTrue(stateChanged);

        //
        // the release metadata should be incremented to the next snapshot
        //

        MavenProject mp2 = new MavenProject(testPom);
        assertEquals(new Version("1.2.3-SNAPSHOT-5"), mp2.getVersion());
    }

    @Test
    public void failedExecution_SnapshotRelease_DotVersionOnDisk() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        File testPom = new File(new File(scratchDirectory, "test"), "test-pom.xml");
        assertTrue(Files.mkdir(testPom.getParentFile()));
        assertTrue(Files.cp(new File(baseDirectory, "src/test/resources/data/maven/pom-sample-dot.xml"), testPom));

        MavenProject mp = new MavenProject(testPom);

        CompletionSequence s = new CompletionSequence();

        //
        // snapshot release, the version on disk is dot
        //
        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, ReleaseMode.snapshot, false, null);


        assertTrue(mp.getVersion().isDot());
        assertEquals(new Version("1.2.3"), mp.getVersion());

        //
        //
        //

        try {

            //
            // we cannot have a dot version on disk in completion phase for a snapshot release
            //

            s.execute(c);
            fail("should throw exception");
        }
        catch(IllegalStateException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("current version cannot be a dot version (1.2.3) for a snapshot release", msg);
        }

        //
        // the release metadata should stay the same
        //

        MavenProject mp2 = new MavenProject(testPom);
        assertEquals(new Version("1.2.3"), mp2.getVersion());
    }

    @Test
    public void successfulExecution_DotRelease_DotVersionOnDisk() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        File testPom = new File(new File(scratchDirectory, "test"), "test-pom.xml");
        assertTrue(Files.mkdir(testPom.getParentFile()));
        assertTrue(Files.cp(new File(baseDirectory, "src/test/resources/data/maven/pom-sample-dot.xml"), testPom));

        MavenProject mp = new MavenProject(testPom);

        CompletionSequence s = new CompletionSequence();

        //
        // dot release, the version on disk is dot
        //
        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, ReleaseMode.patch, false, null);

        assertTrue(mp.getVersion().isDot());
        assertEquals(new Version("1.2.3"), mp.getVersion());

        //
        //
        //

        boolean stateChanged = s.execute(c);

        assertTrue(stateChanged);

        //
        // the release metadata should be incremented to the next snapshot of the next patch release
        //

        MavenProject mp2 = new MavenProject(testPom);
        assertEquals(new Version("1.2.4-SNAPSHOT-1"), mp2.getVersion());
    }

    @Test
    public void failedExecution_DotRelease_SnapshotVersionOnDisk() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        File testPom = new File(new File(scratchDirectory, "test"), "test-pom.xml");
        assertTrue(Files.mkdir(testPom.getParentFile()));
        assertTrue(Files.cp(new File(baseDirectory, "src/test/resources/data/maven/pom-sample-snapshot.xml"), testPom));

        MavenProject mp = new MavenProject(testPom);

        CompletionSequence s = new CompletionSequence();

        //
        // dot release, the version on disk is snapshot
        //
        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, ReleaseMode.patch, false, null);

        assertTrue(mp.getVersion().isSnapshot());
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), mp.getVersion());

        //
        //
        //

        try {

            //
            // we cannot have a snapshot version on disk in completion phase for a dot release
            //

            s.execute(c);
            fail("should throw exception");
        }
        catch(IllegalStateException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("current version cannot be a snapshot version (1.2.3-SNAPSHOT-4) for a dot release", msg);
        }

        //
        // the release metadata should stay the same
        //

        MavenProject mp2 = new MavenProject(testPom);
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), mp2.getVersion());
    }

    // incrementVersionIfNecessary() -----------------------------------------------------------------------------------

    @Test
    public void incrementVersionIfNecessary_SnapshotRelease() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        MockProject mp = new MockProject("1-SNAPSHOT-1");
        ReleaseMode rm = ReleaseMode.snapshot;

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, rm, true, null);

        CompletionSequence s = new CompletionSequence();

        s.incrementVersionIfNecessary(c);

        assertEquals(new Version("1-SNAPSHOT-2"), c.getCurrentVersion());

        List<Version> savedVersionHistory = mp.getSavedVersionHistory();
        assertEquals(1, savedVersionHistory.size());
        assertEquals(new Version("1-SNAPSHOT-2"), savedVersionHistory.get(0));

        assertTrue(s.didExecuteChangeState());
    }

    @Test
    public void incrementVersionIfNecessary_DotRelease() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);
        MockProject mp = new MockProject("1.0.1");
        ReleaseMode rm = ReleaseMode.patch;

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, rm, true, null);

        CompletionSequence s = new CompletionSequence();

        s.incrementVersionIfNecessary(c);

        assertEquals(new Version("1.0.2-SNAPSHOT-1"), c.getCurrentVersion());

        List<Version> savedVersionHistory = mp.getSavedVersionHistory();
        assertEquals(1, savedVersionHistory.size());
        assertEquals(new Version("1.0.2-SNAPSHOT-1"), savedVersionHistory.get(0));

        assertTrue(s.didExecuteChangeState());
    }

    @Test
    public void incrementVersionIfNecessary_CustomSnapshotRelease() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        String versionBeingReleased = "1-SNAPSHOT-1";
        MockProject mp = new MockProject(versionBeingReleased);
        ReleaseMode rm = ReleaseMode.custom;
        rm.setCustomLabel(versionBeingReleased);

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, rm, true, null);

        CompletionSequence s = new CompletionSequence();

        s.incrementVersionIfNecessary(c);

        assertEquals(new Version("1-SNAPSHOT-2"), c.getCurrentVersion());

        List<Version> savedVersionHistory = mp.getSavedVersionHistory();
        assertEquals(1, savedVersionHistory.size());
        assertEquals(new Version("1-SNAPSHOT-2"), savedVersionHistory.get(0));

        assertTrue(s.didExecuteChangeState());
    }

    @Test
    public void incrementVersionIfNecessary_CustomDotRelease() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(mc);

        String versionBeingReleased = "1.2";
        MockProject mp = new MockProject(versionBeingReleased);
        ReleaseMode rm = ReleaseMode.custom;
        rm.setCustomLabel(versionBeingReleased);

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, rm, true, null);

        CompletionSequence s = new CompletionSequence();

        s.incrementVersionIfNecessary(c);

        assertEquals(new Version("1.2.1-SNAPSHOT-1"), c.getCurrentVersion());

        List<Version> savedVersionHistory = mp.getSavedVersionHistory();
        assertEquals(1, savedVersionHistory.size());
        assertEquals(new Version("1.2.1-SNAPSHOT-1"), savedVersionHistory.get(0));

        assertTrue(s.didExecuteChangeState());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected CompletionSequence getSequenceToTest() throws Exception {

        return new CompletionSequence();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
