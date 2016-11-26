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

import io.novaordis.release.model.ArtifactType;
import io.novaordis.release.model.MockArtifact;
import io.novaordis.release.version.Version;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class MavenModuleTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void getArtifactType() throws Exception {

        MockPOM mp = new MockPOM();
        mp.setParent(new MockPOM());
        mp.setArtifactType(ArtifactType.JAR_LIBRARY);

        MavenModule m = new MavenModule(mp);

        assertEquals(ArtifactType.JAR_LIBRARY, m.getArtifactType());
    }

    @Test
    public void getArtifact() throws Exception {

        MockPOM mp = new MockPOM();
        mp.setParent(new MockPOM());
        MockArtifact ma = new MockArtifact(null, null);
        mp.setArtifact(ma);

        MavenModule m = new MavenModule(mp);
        assertEquals(ma, m.getArtifact());
    }

    // name ------------------------------------------------------------------------------------------------------------

    @Test
    public void getName() throws Exception {

        String moduleName = "test-module";

        MockPOM mockPOM = new MockPOM();
        mockPOM.setFile(new File("some/thing/" + moduleName + "/pom.xml"));
        mockPOM.setParent(new MockPOM());
        MavenModule m = new MavenModule(mockPOM);

        assertEquals("test-module", m.getName());
    }

    // setVersion() ----------------------------------------------------------------------------------------------------

    @Test
    public void setVersion() throws Exception {

        MockPOM mockPOM = new MockPOM();
        mockPOM.setParent(new MockPOM());
        MavenModule m = new MavenModule(mockPOM);

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
