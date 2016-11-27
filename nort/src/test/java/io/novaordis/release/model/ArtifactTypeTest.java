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

package io.novaordis.release.model;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class ArtifactTypeTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ArtifactTypeTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void extensions() throws Exception {

        assertEquals("zip", ArtifactType.BINARY_DISTRIBUTION.getExtension());
        assertEquals("jar", ArtifactType.JAR_LIBRARY.getExtension());
    }

    @Test
    public void fromString_jar() throws Exception {

        ArtifactType t = ArtifactType.fromString("jar");
        assertEquals(ArtifactType.JAR_LIBRARY, t);
    }

    @Test
    public void fromString_zip() throws Exception {

        ArtifactType t = ArtifactType.fromString("zip");
        assertEquals(ArtifactType.BINARY_DISTRIBUTION, t);
    }

    @Test
    public void fromString_pom() throws Exception {

        ArtifactType t = ArtifactType.fromString("pom");
        assertNull(t);
    }

    @Test
    public void fromString_UnknownArtifactType() throws Exception {

        try {

            ArtifactType.fromString("something");
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("cannot convert \"something\" to ArgumentType", msg);
        }
    }

    @Test
    public void fromString_Null() throws Exception {

        try {

            ArtifactType.fromString(null);
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null packaging", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
