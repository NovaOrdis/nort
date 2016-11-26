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

import io.novaordis.release.version.Version;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/18/16
 */
public class SequenceExecutionContextTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // generic state ---------------------------------------------------------------------------------------------------

    @Test
    public void nullGenericState() throws Exception {

        SequenceExecutionContext c = new SequenceExecutionContext(null, null, null, null, false, null);

        assertNull(c.get("something"));
        assertNull(c.get(null));
    }

    @Test
    public void genericState() throws Exception {

        SequenceExecutionContext c = new SequenceExecutionContext(null, null, null, null, false, null);

        c.set("something", "somethingelse");
        assertEquals("somethingelse", c.get("something"));
    }

    // typed access - tests were executed ------------------------------------------------------------------------------

    @Test
    public void typedAccess_wereTestsExecuted() throws Exception {

        SequenceExecutionContext c = new SequenceExecutionContext(null, null, null, null, false, null);

        assertFalse(c.wereTestsExecuted());

        c.setTestsExecuted(true);

        assertTrue(c.wereTestsExecuted());
    }

    @Test
    public void typedAccess_getCurrentVersion() throws Exception {

        SequenceExecutionContext c = new SequenceExecutionContext(null, null, null, null, false, null);

        assertNull(c.getCurrentVersion());

        c.setCurrentVersion(new Version("1.2.3"));

        assertEquals(new Version("1.2.3"), c.getCurrentVersion());
    }

    // typed access - current version ----------------------------------------------------------------------------------


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
