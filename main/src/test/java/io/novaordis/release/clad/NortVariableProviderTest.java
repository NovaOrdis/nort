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

package io.novaordis.release.clad;

import io.novaordis.utilities.env.EnvironmentVariableProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 12/20/16
 */
public class NortVariableProviderTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(NortVariableProviderTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // environment variable provider -----------------------------------------------------------------------------------

    @Test
    public void environmentVariableProvider() {

        NortVariableProvider p = new NortVariableProvider();

        EnvironmentVariableProvider original = p.getEnvironmentVariableProvider();

        assertNotNull(original);

        MockEnvironmentVariableProvider p2 = new MockEnvironmentVariableProvider();

        p.setEnvironmentVariableProvider(p2);

        EnvironmentVariableProvider p3 = p.getEnvironmentVariableProvider();

        assertEquals(p3, p2);
        assertNotEquals(p3, original);

        //
        // we don't have a parent
        //
        assertNull(p.getVariableProviderParent());

        try {

            p.setVariableProviderParent(new MockVariableProvider());
            fail("should have thrown exception");
        }
        catch(UnsupportedOperationException e) {

            log.info("" + e);
        }
    }

    @Test
    public void resolveAnEnvironmentVariable() {

        MockEnvironmentVariableProvider mp = new MockEnvironmentVariableProvider();

        NortVariableProvider p = new NortVariableProvider();
        p.setEnvironmentVariableProvider(mp);

        assertNull(p.getVariableValue("NO_SUCH_VARIABLE"));

        String value = "some value";
        String variableName = "SOME_VARIABLE";
        mp.installEnvironmentVariable(variableName, value);

        String s = p.getVariableValue(variableName);
        assertEquals("some value", s);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
