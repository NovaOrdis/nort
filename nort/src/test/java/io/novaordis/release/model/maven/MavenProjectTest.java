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
import io.novaordis.release.model.ProjectTest;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class MavenProjectTest extends ProjectTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MavenProjectTest.class);

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
    public void uninitialized() throws Exception {

        MavenProject mp = new MavenProject();

        assertNull(mp.getVersion());
        assertNull(mp.getFile());
        assertNull(mp.getBaseDirectory());
        assertTrue(mp.getArtifactTypes().isEmpty());

        //
        // attempt to save
        //
        try {

            mp.save();
            fail("should have thrown exception");
        }
        catch(IOException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("attempt to save an uninitialized project instance", msg);
        }

        assertFalse(mp.undo());
    }

    @Test
    public void accessors() throws Exception {

        File file = Util.cp(baseDirectory, "src/test/resources/data/maven/pom-sample.xml", scratchDirectory, "pom.xml");

        MavenProject p = new MavenProject(file);

        assertEquals(file, p.getFile());
        assertEquals(scratchDirectory, p.getBaseDirectory());
        assertEquals(new Version("1.2.3"), p.getVersion());

        Set<ArtifactType> ts = p.getArtifactTypes();
        assertEquals(1, ts.size());
        assertTrue(ts.contains(ArtifactType.JAR_LIBRARY));

        File relativeJarLibraryFile = p.getArtifacts(ArtifactType.JAR_LIBRARY).get(0).getRepositoryFile();
        File expected = new File("io/novaordis/example-group/example-artifact/1.2.3/example-artifact-1.2.3.jar");

        assertEquals(expected, relativeJarLibraryFile);

        //
        // there is no binary
        //
        assertTrue(p.getArtifacts(ArtifactType.BINARY_DISTRIBUTION).isEmpty());
    }

    @Test
    public void constructor_MultiModules_ModulesCannotBeResolved() throws Exception {

        File pf = Util.cp(
                baseDirectory, "src/test/resources/data/maven/multi-module-project/pom.xml",
                scratchDirectory, "multi-module-pom.xml");

        try {
            new MavenProject(pf);
            fail("should throw exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("no module directory .*/module1"));
        }
    }

    @Test
    public void moduleRelatedAccessors() throws Exception {

        File pd = Util.cp(baseDirectory, "src/test/resources/data/maven/multi-module-project", scratchDirectory);

        File pf = new File(pd, "pom.xml");
        assertTrue(pf.isFile());

        MavenProject p = new MavenProject(pf);

        Set<ArtifactType> ts = p.getArtifactTypes();
        assertEquals(2, ts.size());
        assertTrue(ts.contains(ArtifactType.JAR_LIBRARY));
        assertTrue(ts.contains(ArtifactType.BINARY_DISTRIBUTION));

        List<Artifact> artifacts = p.getArtifacts();
        assertEquals(3, artifacts.size());

        Artifact a = artifacts.get(0);
        assertEquals(a, p.getArtifacts(ArtifactType.JAR_LIBRARY).get(0));
        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());
        assertEquals(new File("io/test/module1-artifact/1.0/module1-artifact-1.0.jar"), a.getRepositoryFile());

        Artifact a2 = artifacts.get(1);
        assertEquals(a2, p.getArtifacts(ArtifactType.JAR_LIBRARY).get(1));
        assertEquals(ArtifactType.JAR_LIBRARY, a2.getType());
        assertEquals(new File("io/test/module2-artifact/2.0/module2-artifact-2.0.jar"), a2.getRepositoryFile());

        Artifact a3 = artifacts.get(2);
        assertEquals(a3, p.getArtifacts(ArtifactType.BINARY_DISTRIBUTION).get(0));
        assertEquals(ArtifactType.BINARY_DISTRIBUTION, a3.getType());
        assertEquals(new File("io/test/release-artifact/3.0/release-artifact-3.0.zip"), a3.getRepositoryFile());
    }

    @Test
    public void set_save_undo() throws Exception {

        File file = new File(scratchDirectory, "test-pom.xml");
        Files.cp(new File(System.getProperty("basedir"), "src/test/resources/data/maven/pom-sample.xml"), file);

        MavenProject model = new MavenProject(file);

        assertEquals(new Version("1.2.3"), model.getVersion());
        assertTrue(model.setVersion(new Version("3.2.1")));

        assertTrue(model.save());

        MavenProject model2 = new MavenProject(file);
        assertEquals(new Version("3.2.1"), model2.getVersion());

        assertTrue(model.undo());

        MavenProject model3 = new MavenProject(file);
        assertEquals(new Version("1.2.3"), model3.getVersion());
    }

    // artifact management ---------------------------------------------------------------------------------------------

    @Test
    public void artifacts_JAR_LIBRARY() throws Exception {

        File pom = Util.cp(baseDirectory, "src/test/resources/data/maven/pom-sample.xml", scratchDirectory, "pom.xml");

        MavenProject mp = new MavenProject(pom);

        //
        // get artifacts
        //

        List<Artifact> artifacts = mp.getArtifacts();
        assertEquals(1, artifacts.size());

        Artifact a = artifacts.get(0);

        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());
        assertEquals(
                new File("io/novaordis/example-group/example-artifact/1.2.3/example-artifact-1.2.3.jar"),
                a.getRepositoryFile());

        //
        // change version
        //

        assertTrue(mp.setVersion(new Version("2")));

        //
        // get artifacts again
        //

        Artifact a2 = mp.getArtifacts().get(0);

        assertEquals(
                new File("io/novaordis/example-group/example-artifact/2/example-artifact-2.jar"),
                a2.getRepositoryFile());
    }

    @Test
    public void artifacts_BINARY_DISTRIBUTION() throws Exception {

        File pom = Util.cp(baseDirectory, "src/test/resources/data/maven/pom-zip-sample.xml", scratchDirectory, "pom.xml");

        MavenProject mp = new MavenProject(pom);

        //
        // get artifacts
        //

        List<Artifact> artifacts = mp.getArtifacts();
        assertEquals(1, artifacts.size());

        Artifact a = artifacts.get(0);

        assertEquals(ArtifactType.BINARY_DISTRIBUTION, a.getType());
        assertEquals(
                new File("io/novaordis/example-group/example-artifact/1.2.3/example-artifact-1.2.3.zip"),
                a.getRepositoryFile());

        //
        // change version
        //

        assertTrue(mp.setVersion(new Version("2")));

        //
        // get artifacts again
        //

        Artifact a2 = mp.getArtifacts().get(0);

        assertEquals(
                new File("io/novaordis/example-group/example-artifact/2/example-artifact-2.zip"),
                a2.getRepositoryFile());
    }

    @Test
    public void artifacts_MULTI_MODULE_PROJECT() throws Exception {

        File pd = Util.cp(baseDirectory, "src/test/resources/data/maven/multi-module-project", scratchDirectory);
        File pf = new File(pd, "pom.xml");
        assertTrue(pf.isFile());

        MavenProject p = new MavenProject(pf);

        Artifact a = p.getArtifacts(ArtifactType.BINARY_DISTRIBUTION).get(0);
        assertEquals(new File("io/test/release-artifact/3.0/release-artifact-3.0.zip"), a.getRepositoryFile());

        assertTrue(p.getModule("release").setVersion(new Version("77.77")));

        Artifact a2 = p.getArtifacts(ArtifactType.BINARY_DISTRIBUTION).get(0);
        assertEquals(new File("io/test/release-artifact/77.77/release-artifact-77.77.zip"), a2.getRepositoryFile());
    }

    // Module management -----------------------------------------------------------------------------------------------

    @Test
    public void getModule() throws Exception {

        MavenProject p = new MavenProject();

        MockPOM parent = new MockPOM();
        MockPOM modulePom = new MockPOM();
        modulePom.setParent(parent);
        modulePom.setFile(new File("a/b/test/pom.xml"));

        MavenModule module = new MavenModule(modulePom);

        p.addModule(module);

        assertEquals(module, p.getModule("test"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected MavenProject getProjectToTest() throws Exception {

        File file = new File(scratchDirectory, "test-pom.xml");
        assertTrue(Files.cp(
                new File(System.getProperty("basedir"), "src/test/resources/data/maven/pom-sample.xml"), file));

        return new MavenProject(file);
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
