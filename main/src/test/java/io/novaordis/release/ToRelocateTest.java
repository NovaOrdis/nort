/*
 * Copyright (c) 2017 Nova Ordis LLC
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

package io.novaordis.release;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/20/17
 */
public class ToRelocateTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ToRelocateTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // toBoolean -------------------------------------------------------------------------------------------------------

    @Test
    public void toBoolean_Null() throws Exception {

        assertFalse(ToRelocate.toBoolean(null));
    }

    @Test
    public void toBoolean_InvalidValue() throws Exception {

        try {

            ToRelocate.toBoolean("something");
            fail("should throw Exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("\"something\" is not a valid boolean value", msg);
        }
    }

    @Test
    public void toBoolean_False() throws Exception {

        assertFalse(ToRelocate.toBoolean("false"));
    }

    @Test
    public void toBoolean_False_2() throws Exception {

        assertFalse(ToRelocate.toBoolean("False"));
    }

    @Test
    public void toBoolean_True() throws Exception {

        assertTrue(ToRelocate.toBoolean("true"));
    }

    @Test
    public void toBoolean_True_2() throws Exception {

        assertTrue(ToRelocate.toBoolean("True"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
