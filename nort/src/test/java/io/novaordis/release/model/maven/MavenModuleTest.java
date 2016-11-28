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
import io.novaordis.release.model.MockArtifact;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class MavenModuleTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MavenModuleTest.class);

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

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullProject() throws Exception {

        try {

            new MavenModule(null, new File(""));
            fail("should throw exception");
        }
        catch(NullPointerException e) {
            String msg = e.getMessage();
            log.info(msg);
        }
    }

    @Test
    public void constructor() throws Exception {

        File f = Util.cp(
                baseDirectory, "src/test/resources/data/maven/lockstep-multi-module-project/module1/pom.xml",
                scratchDirectory);

        MockMavenProject mp = new MockMavenProject();
        MockPOM rootPom = new MockPOM();
        mp.setPOM(rootPom);
        mp.setVersion(new Version("3.3.3"));

        MavenModule m = new MavenModule(mp, f);

        Artifact a = m.getArtifact();

        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());
        assertEquals(new File("io/test/module1-artifact/3.3.3/module1-artifact-3.3.3.jar"), a.getRepositoryFile());
        assertEquals("test-scratch", m.getName());
        assertEquals(ArtifactType.JAR_LIBRARY, m.getArtifactType());
        assertEquals(mp, m.getProject());
        Version v = m.getVersion();
        assertEquals(new Version("3.3.3"), v);

        POM pom = m.getPOM();
        assertEquals(f, pom.getFile());
    }

    // getArtifactType() -----------------------------------------------------------------------------------------------

    @Test
    public void getArtifactType() throws Exception {

        MockMavenProject mmp = new MockMavenProject();
        mmp.setPOM(new MockPOM());

        MockPOM mockModulePom = new MockPOM();
        mockModulePom.setParent(mmp.getPOM());
        mockModulePom.setArtifactType(ArtifactType.JAR_LIBRARY);

        MavenModule m = new MavenModule(mmp, mockModulePom);

        assertEquals(ArtifactType.JAR_LIBRARY, m.getArtifactType());
    }

    @Test
    public void getArtifact() throws Exception {

        MockMavenProject mmp = new MockMavenProject();
        mmp.setPOM(new MockPOM());

        MockPOM mockModulePom = new MockPOM();
        mockModulePom.setParent(mmp.getPOM());

        MockArtifact ma = new MockArtifact(null, null, null);
        mockModulePom.setArtifact(ma);

        MavenModule m = new MavenModule(mmp, mockModulePom);

        assertEquals(ma, m.getArtifact());
    }

    // name ------------------------------------------------------------------------------------------------------------

    @Test
    public void getName() throws Exception {

        MockMavenProject mmp = new MockMavenProject();
        mmp.setPOM(new MockPOM());

        MockPOM mockModulePom = new MockPOM();
        mockModulePom.setParent(mmp.getPOM());

        String moduleName = "test-module";
        mockModulePom.setFile(new File("some/thing/" + moduleName + "/pom.xml"));

        MavenModule m = new MavenModule(mmp, mockModulePom);

        assertEquals("test-module", m.getName());
    }

    // setVersion() ----------------------------------------------------------------------------------------------------

    @Test
    public void setVersion_LockstepVersioningModel() throws Exception {

        MockMavenProject mmp = new MockMavenProject();
        mmp.setVersioningModel(ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP);
        mmp.setPOM(new MockPOM());

        MockPOM mockModulePom = new MockPOM();
        mockModulePom.setParent(mmp.getPOM());

        MavenModule m = new MavenModule(mmp, mockModulePom);

        try {

            m.setVersion(new Version("1"));
            fail("should throw exception");
        }
        catch(UnsupportedOperationException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals(
                    "cannot independently set version on modules in " + ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP +
                            " versioning mode", msg);
        }
    }

    @Test
    public void setVersion_IndependentVersioningModel() throws Exception {

        MockMavenProject mmp = new MockMavenProject();
        mmp.setVersioningModel(ProjectVersioningModel.MULTIPLE_MODULE_INDEPENDENT);
        mmp.setPOM(new MockPOM());

        MockPOM mockModulePom = new MockPOM();
        mockModulePom.setParent(mmp.getPOM());
        mockModulePom.setVersion(new Version("1"));

        MavenModule m = new MavenModule(mmp, mockModulePom);

        Version v = m.getVersion();
        assertEquals(new Version("1"), v);
        assertFalse(m.setVersion(new Version("1")));
        assertTrue(m.setVersion(new Version("2")));
        assertEquals(new Version("2"), m.getVersion());
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
