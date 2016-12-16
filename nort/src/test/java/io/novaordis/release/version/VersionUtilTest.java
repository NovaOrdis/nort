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

package io.novaordis.release.version;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 12/16/16
 */
public class VersionUtilTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(VersionUtilTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // fromCanonicalString() -------------------------------------------------------------------------------------------

    @Test
    public void fromCanonicalString_Null() throws Exception {

        try {
            VersionUtil.fromCanonicalString(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null argument", msg);
        }
    }

    @Test
    public void fromCanonicalString() throws Exception {

        String s = "1.0.0";

        Version v = VersionUtil.fromCanonicalString(s);

        assertEquals(new Version("1.0.0"), v);
    }

    @Test
    public void fromCanonicalString_NeedsTrimming() throws Exception {

        String s = "   1.0.0 \t\t ";

        Version v = VersionUtil.fromCanonicalString(s);

        assertEquals(new Version("1.0.0"), v);
    }

    @Test
    public void fromCanonicalString_InvalidVersion() throws Exception {

        try {
            VersionUtil.fromCanonicalString("not a version");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null argument", msg);
        }
    }

    // fromCommandStdout() ---------------------------------------------------------------------------------------------

    @Test
    public void fromCommandStdout_Null() throws Exception {

        try {
            VersionUtil.fromCommandStdout(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null argument", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
