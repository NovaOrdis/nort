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

import io.novaordis.release.model.ArtifactTest;
import io.novaordis.release.model.ArtifactType;
import io.novaordis.release.version.Version;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public abstract class MavenArtifactTest extends ArtifactTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        MockPOM mp = new MockPOM();

        MavenArtifactImpl a = new MavenArtifactImpl(mp, ArtifactType.JAR_LIBRARY,
                "io.test-group", "test-artifact", new Version("1.0"), null, null);

        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());
        assertEquals(new File("io/test-group/test-artifact/1.0/test-artifact-1.0.jar"), a.getRepositoryFile());

        //
        // root POM
        //
        assertNull(mp.getModule());
        assertEquals(new File("target/test-artifact-1.0.jar"), a.getLocalFile());

        //
        // module POM
        //
        MockMavenModule mm = MockMavenModule.getInstance(mp);
        mm.setName("blue");

        assertEquals(new File("blue/target/test-artifact-1.0.jar"), a.getLocalFile());
    }

    @Test
    public void constructor_FinalNameSpecified_JAR() throws Exception {

        MockPOM mp = new MockPOM();

        MavenArtifactImpl a = new MavenArtifactImpl(mp, ArtifactType.JAR_LIBRARY,
                "io.test-group", "test-artifact", new Version("1.0"), "blue", null);

        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());

        //
        // finalName is ignored when building files to be installed in repository
        // https://kb.novaordis.com/index.php/Maven_pom.xml#.3CfinalName.3E
        //

        assertEquals(new File("io/test-group/test-artifact/1.0/test-artifact-1.0.jar"), a.getRepositoryFile());

        //
        // root POM
        //
        assertNull(mp.getModule());
        assertEquals(new File("target/blue.jar"), a.getLocalFile());

        //
        // module POM
        //
        MockMavenModule mm = MockMavenModule.getInstance(mp);
        mm.setName("red");

        assertEquals(new File("red/target/blue.jar"), a.getLocalFile());
    }

    @Test
    public void constructor_FinalNameSpecified_BINARY_DISTRIBUTION() throws Exception {

        MockPOM mp = new MockPOM();

        MavenArtifactImpl a = new MavenArtifactImpl(mp, ArtifactType.BINARY_DISTRIBUTION,
                "io.test-group", "release", new Version("1.0.0-SNAPSHOT-4"), "red", "zip");

        assertEquals(ArtifactType.BINARY_DISTRIBUTION, a.getType());

        //
        // finalName is ignored when building files to be installed in repository
        // https://kb.novaordis.com/index.php/Maven_pom.xml#.3CfinalName.3E
        //

        assertEquals(new File("io/test-group/release/1.0.0-SNAPSHOT-4/release-1.0.0-SNAPSHOT-4.zip"),
                a.getRepositoryFile());

        //
        // root POM
        //
        assertNull(mp.getModule());
        assertEquals(new File("target/red.zip"), a.getLocalFile());

        //
        // module POM
        //
        MockMavenModule mm = MockMavenModule.getInstance(mp);
        mm.setName("blue");

        assertEquals(new File("blue/target/red.zip"), a.getLocalFile());

    }


    // equals() --------------------------------------------------------------------------------------------------------

    @Test
    public void equals() throws Exception {

        MockPOM mp = new MockPOM();
        MockPOM mp2 = new MockPOM();

        MavenArtifactImpl a = new MavenArtifactImpl(mp, ArtifactType.JAR_LIBRARY, "io.test", "test-artifact",
                new Version("1.0"), null, null);
        MavenArtifactImpl a2 = new MavenArtifactImpl(mp2, ArtifactType.JAR_LIBRARY, "io.test", "test-artifact",
                new Version("1.0"), null, null);

        assertEquals(a, a2);
        assertEquals(a2, a);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected abstract MavenArtifact getArtifactToTest() throws Exception;

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
