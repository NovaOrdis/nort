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

package io.novaordis.release.model.maven;

import io.novaordis.release.Util;
import io.novaordis.release.model.Artifact;
import io.novaordis.release.model.ArtifactType;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class POMTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(POMTest.class);

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
    }

    @After
    public void after() throws Exception {

        //
        // scratch directory cleanup
        //

        assertTrue(Files.rmdir(scratchDirectory, false));
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NoGroupID() throws Exception {

        File file = Util.cp(
                baseDirectory, "src/test/resources/data/maven/pom-no-group-id.xml", scratchDirectory, "pom.xml");

        try {

            new POM(file);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("missing groupId", msg);
        }
    }

    @Test
    public void constructor_NoGroupID2() throws Exception {

        File file = Util.cp(
                baseDirectory, "src/test/resources/data/maven/pom-no-group-id.xml", scratchDirectory, "pom.xml");

        MockPOM mockParent = new MockPOM();
        assertNull(mockParent.getGroupId());

        try {

            new POM(mockParent, file);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("missing groupId", msg);
        }
    }

    @Test
    public void constructorWithParent() throws Exception {

        File file = Util.cp(
                baseDirectory, "src/test/resources/data/maven/pom-sample.xml", scratchDirectory, "pom.xml");

        MockPOM mockParent = new MockPOM();
        POM p = new POM(mockParent, file);

        assertEquals(mockParent, p.getParent());
        assertEquals("io.novaordis.example-group", p.getGroupId());
    }

    @Test
    public void constructor_NoGroupId_ParentHasGroupId() throws Exception {

        File file = Util.cp(
                baseDirectory, "src/test/resources/data/maven/pom-no-group-id.xml", scratchDirectory, "pom.xml");

        MockPOM mockParent = new MockPOM();
        mockParent.setGroupId("io.test.group");
        POM p = new POM(mockParent, file);

        assertEquals(mockParent, p.getParent());
        assertEquals("io.test.group", p.getGroupId());
    }

    @Test
    public void accessors() throws Exception {

        File file = new File(baseDirectory, "src/test/resources/data/maven/pom-sample.xml");
        assertTrue(file.isFile());

        POM p = new POM(file);

        assertEquals(file, p.getFile());
        assertEquals(new Version("1.2.3"), p.getVersion());
        assertEquals(ArtifactType.JAR_LIBRARY, p.getArtifactType());

        Artifact artifact = p.getArtifact();

        assertEquals(ArtifactType.JAR_LIBRARY, artifact.getType());

        File artifactFile = artifact.getRepositoryFile();
        assertEquals(
                new File("io/novaordis/example-group/example-artifact/1.2.3/example-artifact-1.2.3.jar"), artifactFile);

        assertNull(p.getParent());
    }

    @Test
    public void versionChange() throws Exception {

        File orig = new File(baseDirectory, "src/test/resources/data/maven/pom-sample.xml");
        File f = new File(scratchDirectory, "pom.xml");
        assertTrue(Files.cp(orig, f));

        POM p = new POM(f);

        assertEquals(new Version("1.2.3"), p.getVersion());
        Artifact a = p.getArtifact();
        assertEquals(new File("io/novaordis/example-group/example-artifact/1.2.3/example-artifact-1.2.3.jar"),
                a.getRepositoryFile());

        assertTrue(p.setVersion(new Version("3.2.1")));
        Artifact a2 = p.getArtifact();
        assertEquals(new File("io/novaordis/example-group/example-artifact/3.2.1/example-artifact-3.2.1.jar"),
                a2.getRepositoryFile());
    }

    @Test
    public void multiModulePom() throws Exception {

        File pomFile = Util.cp(
                baseDirectory, "src/test/resources/data/maven/multi-module-project/pom.xml",
                scratchDirectory, "pom.xml");

        POM pom = new POM(pomFile);

        assertNull(pom.getArtifact());
        assertNull(pom.getArtifactType());

        Version v = pom.getVersion();
        assertEquals(new Version("0"), v);

        File f = pom.getFile();
        assertEquals(pomFile, f);

        List<String> moduleNames = pom.getModuleNames();
        assertEquals(3, moduleNames.size());
        assertEquals("module1", moduleNames.get(0));
        assertEquals("module2", moduleNames.get(1));
        assertEquals("release", moduleNames.get(2));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
