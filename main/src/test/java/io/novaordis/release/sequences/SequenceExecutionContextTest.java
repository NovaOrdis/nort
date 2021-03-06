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

import io.novaordis.release.MockConfiguration;
import io.novaordis.release.MockReleaseApplicationRuntime;
import io.novaordis.release.model.MockProject;
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

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(new MockConfiguration());

        SequenceExecutionContext c = new SequenceExecutionContext(mr, null, null, null);

        assertNull(c.get("something"));
        assertNull(c.get(null));
    }

    @Test
    public void genericState() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(new MockConfiguration());

        SequenceExecutionContext c = new SequenceExecutionContext(mr, null, null, null);

        c.set("something", "somethingelse");
        assertEquals("somethingelse", c.get("something"));
    }

    // current version -------------------------------------------------------------------------------------------------

    @Test
    public void insureCurrentVersionIsInitializedOnConstruction() throws Exception {

        MockProject mp = new MockProject("1.2.3-SNAPSHOT-4");

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(new MockConfiguration());

        SequenceExecutionContext c = new SequenceExecutionContext(mr, mp, null, null);

        Version v = c.getCurrentVersion();
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), v);
    }

    @Test
    public void setCurrentVersion_ValidValue() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        SequenceExecutionContext c = new SequenceExecutionContext(mr, null, null, null);
        c.setCurrentVersion(new Version("6.7.8"));
        assertEquals(new Version("6.7.8"), c.getCurrentVersion());
    }

    @Test
    public void setCurrentVersion_Null() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        SequenceExecutionContext c = new SequenceExecutionContext(mr, null, null, null);
        c.setCurrentVersion(new Version("6.7.8"));
        assertEquals(new Version("6.7.8"), c.getCurrentVersion());

        c.setCurrentVersion(null);
        assertNull(c.getCurrentVersion());
    }

    // typed access - tests were executed ------------------------------------------------------------------------------

    @Test
    public void typedAccess_wereTestsExecuted() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(new MockConfiguration());

        SequenceExecutionContext c = new SequenceExecutionContext(mr, null, null, null);

        assertFalse(c.wereTestsExecuted());

        c.setTestsExecuted(true);

        assertTrue(c.wereTestsExecuted());
    }

    @Test
    public void typedAccess_getCurrentVersion() throws Exception {

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime(new MockConfiguration());

        SequenceExecutionContext c = new SequenceExecutionContext(mr, null, null, null);

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
