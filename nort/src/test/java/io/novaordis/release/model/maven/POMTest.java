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
import io.novaordis.utilities.variable.VariableProvider;
import io.novaordis.utilities.xml.editor.BasicInLineXmlEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
                baseDirectory, "src/test/resources/data/maven/pom-parent-has-group-id.xml", scratchDirectory, "pom.xml");

        MockPOM mockParent = new MockPOM();
        mockParent.setGroupId("io.test.group");
        POM p = new POM(mockParent, file);

        assertEquals(mockParent, p.getParent());
        assertEquals("io.test.group", p.getGroupId());
    }

    @Test
    public void standardPOM() throws Exception {

        File file = new File(baseDirectory, "src/test/resources/data/maven/pom-sample.xml");
        assertTrue(file.isFile());

        POM pom = new POM(file);

        assertEquals(file, pom.getFile());
        assertEquals("example-artifact", pom.getArtifactId());
        assertEquals("io.novaordis.example-group", pom.getGroupId());
        assertEquals(new Version("1.2.3"), pom.getVersion());
        assertEquals(new Version("1.2.3"), pom.getLocalVersion());
        assertEquals(ArtifactType.JAR_LIBRARY, pom.getArtifactType());

        MavenArtifact artifact = pom.getArtifact();
        assertEquals(pom, artifact.getPom());

        assertEquals(ArtifactType.JAR_LIBRARY, artifact.getType());

        File artifactFile = artifact.getRepositoryFile();

        assertEquals(new File(
                "io/novaordis/example-group/example-artifact/1.2.3/example-artifact-1.2.3.jar"), artifactFile);

        assertNull(pom.getParent());

        assertNull(pom.getFinalName());
        assertEquals("jar", pom.getExtension());
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
    public void getVersion_NoVersionSpecifiedInPOM_NoParent() throws Exception {

        File f = Util.cp(baseDirectory,
                "src/test/resources/data/maven/lockstep-multi-module-project/module1/pom.xml", scratchDirectory);

        POM p = new POM(f);

        assertNull(p.getLocalVersion());
        assertNull(p.getVersion());
    }

    @Test
    public void getVersion_NoVersionSpecifiedInPOM_ParentPresent() throws Exception {

        File f = Util.cp(baseDirectory,
                "src/test/resources/data/maven/lockstep-multi-module-project/module1/pom.xml", scratchDirectory);

        MockPOM parent = new MockPOM();
        parent.setVersion(new Version("77.77"));

        POM p = new POM(parent, f);

        Version v = p.getVersion();
        assertEquals(new Version("77.77"), v);
        assertNull(p.getLocalVersion());
    }

    @Test
    public void parentVersionChange() throws Exception {

        File f = Util.cp(baseDirectory,
                "src/test/resources/data/maven/lockstep-multi-module-project/module1/pom.xml", scratchDirectory);

        POM p = new POM(f);

        assertEquals(new Version("88"), p.getParentVersion());

        String content = Files.read(f);

        assertTrue(p.setParentVersion(new Version("99")));

        String content2 = Files.read(f);

        assertEquals(content, content2);

        assertTrue(p.save());

        String s = new BasicInLineXmlEditor(f).get("/project/parent/version");
        assertEquals("99", s);
    }

    // multiple modules ------------------------------------------------------------------------------------------------

    @Test
    public void multiModuleProject() throws Exception {

        File pomFile = Util.cp("maven/lockstep-multi-module-project/pom.xml", scratchDirectory);

        POM pom = new POM(pomFile);

        assertEquals("lockstep-multi-module-project", pom.getArtifactId());
        assertEquals("io.test", pom.getGroupId());

        assertNull(pom.getArtifact());
        assertNull(pom.getArtifactType());

        Version v = pom.getVersion();
        assertEquals(new Version("88"), v);

        assertNull(pom.getFinalName());
        assertNull(pom.getExtension());

        File f = pom.getFile();
        assertEquals(pomFile, f);

        List<String> moduleNames = pom.getModuleNames();
        assertEquals(3, moduleNames.size());
        assertEquals("module1", moduleNames.get(0));
        assertEquals("module2", moduleNames.get(1));
        assertEquals("release", moduleNames.get(2));
    }

    @Test
    public void pomPackaging_NoModules_NoParent() throws Exception {

        File pomFile = Util.
                cp(baseDirectory, "src/test/resources/data/maven/invalid-poms/pom-no-modules-no-parent.xml",
                        scratchDirectory, "pom-no-modules-no-parent.xml");

        try {

            new POM(pomFile);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches(
                    "invalid 'pom' packaging POM file, no modules and no parent .*pom-no-modules-no-parent\\.xml"));
        }
    }

    // release module --------------------------------------------------------------------------------------------------

    @Test
    public void pomPackaging_ReleaseModule_NoAssembly() throws Exception {

        File pomFile = Util.
                cp(baseDirectory, "src/test/resources/data/maven/invalid-poms/pom-release-no-assembly-plugin.xml",
                        scratchDirectory);

        MockPOM parent = new MockPOM();

        try {

            new POM(parent, pomFile);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("the release module '.*' does not contain an assembly plugin"));
        }
    }

    @Test
    public void pomPackaging_ReleaseModule_AssemblyDescriptorNotAccessible() throws Exception {

        File dir = Util.cp(baseDirectory, "src/test/resources/data/maven/lockstep-multi-module-project", scratchDirectory);

        File pomFile = new File(dir, "release/pom.xml");

        //
        // remove the assembly descriptor
        //

        File assemblyDescriptorFile = new File(dir, "release/src/assembly/release.xml");

        assertTrue(assemblyDescriptorFile.delete());
        assertFalse(assemblyDescriptorFile.isFile());

        MockPOM root = new MockPOM();

        try {
            new POM(root, pomFile);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("assembly descriptor .* not available or cannot be read"));
        }
    }

    @Test
    public void pomPackaging_ValidReleaseModule() throws Exception {

        File dir = Util.cp("maven/lockstep-multi-module-project", scratchDirectory);
        File pomFile = new File(dir, "release/pom.xml");
        MockPOM root = new MockPOM();
        root.setVersion(new Version("33.33"));

        POM pom = new POM(root, pomFile);

        assertEquals(ArtifactType.BINARY_DISTRIBUTION, pom.getArtifactType());

        MavenArtifact a = pom.getArtifact();
        assertEquals(pom, a.getPom());

        assertEquals(ArtifactType.BINARY_DISTRIBUTION, a.getType());
        assertEquals(new File("io/test/release/33.33/release-33.33.tar.gz"), a.getRepositoryFile());
    }

    // POM hierarchy ---------------------------------------------------------------------------------------------------

    @Test
    public void parentChildRelationship() throws Exception {

        File f = Util.cp("maven/poms-with-variables/pom-with-variable-1.xml", scratchDirectory);
        POM parent = new POM(f);
        POMVariableProvider parentVp = parent.getVariableProvider();

        assertEquals("A", parentVp.getVariableValue("custom.property.1"));

        File f2 = Util.cp("maven/poms-with-variables/pom-with-variable-2.xml", scratchDirectory);
        POM child = new POM(parent, f2);
        POMVariableProvider childVp = child.getVariableProvider();

        //
        // verify that the variable provider relationship is established, in that we can read parent's properties
        //

        assertEquals("B", childVp.getVariableValue("custom.property.2"));
        assertEquals("A", childVp.getVariableValue("custom.property.1"));
    }

    // variable support ------------------------------------------------------------------------------------------------

    @Test
    public void customVariableSupport() throws Exception {

        File f = Util.cp("maven/poms-with-variables/pom-with-variable-as-custom-property.xml", scratchDirectory);

        POM p = new POM(f);

        VariableProvider provider = p.getVariableProvider();

        String v = provider.getVariableValue("my.version");

        assertEquals("3.2.1", v);

        String s = p.getFinalName();

        assertEquals("blah-3.2.1", s);
    }

    @Test
    public void mavenVariableSupport_Version() throws Exception {

        File f = Util.cp(baseDirectory,
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-as-maven-property.xml",
                scratchDirectory);

        POM p = new POM(f);

        assertEquals("8888", p.getVersion().getLiteral());

        String s = p.getFinalName();

        assertEquals("blah-8888", s);
    }

    @Test
    public void mavenVariableSupport_ProjectVersion() throws Exception {

        File f = Util.cp(baseDirectory,
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-as-maven-property-2.xml",
                scratchDirectory);

        POM p = new POM(f);

        assertEquals("9999", p.getVersion().getLiteral());

        String s = p.getFinalName();

        assertEquals("blah-9999", s);
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
