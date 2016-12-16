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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class NumericVersionTokenTest extends VersionTokenTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(NumericVersionTokenTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void nature() throws Exception {

        NumericVersionToken t = new NumericVersionToken("1");

        assertFalse(t.isDot());
        assertFalse(t.isSnapshotSeparator());
        assertFalse(t.isSeparator());
        assertTrue(t.isNumericComponent());
        assertEquals("1", t.getLiteral());
        assertEquals(1, t.getNumericValue().intValue());
    }

    @Test
    public void notAValidNumericVersionComponent() throws Exception {

        try {
            new NumericVersionToken("a");
            fail("should throw exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid numeric version component \"a\"", msg);
        }
    }

    @Test
    public void negativeVersionComponent() throws Exception {

        try {
            new NumericVersionToken("-1");
            fail("should throw exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("\"-1\" negative version component", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected NumericVersionToken getVersionTokenToTest() {

        try {
            return new NumericVersionToken("1");
        }
        catch(VersionFormatException e) {
            throw new IllegalStateException(e);
        }
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
