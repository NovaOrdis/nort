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

package io.novaordis.release.sequences;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/18/16
 */
public class ExecutionHistoryTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ExecutionHistoryTest.class);


    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void lifecycle() throws Exception {

        ExecutionHistory h = new ExecutionHistory();

        assertEquals(0, h.length());

        MockSequence ms = new MockSequence();

        h.record("update", ms, true, false);

        assertEquals(1, h.length());

        SequenceOperation so = h.getOperation(0);

        assertEquals("update", so.getMethodName());

        assertEquals(ms, so.getTarget());

        assertTrue(so.wasSuccess());

        assertFalse(so.didChangeState());
    }

    @Test
    public void accessOutOfBounds() throws Exception {

        ExecutionHistory h = new ExecutionHistory();

        try {

            h.getOperation(0);
            fail("should have thrown exception");

        }
        catch(IndexOutOfBoundsException e) {

            log.info(e.getMessage());

        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
