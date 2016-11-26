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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class SnapshotSeparatorVersionTokenTest extends VersionTokenTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void nature() throws Exception {

        SnapshotSeparatorVersionToken t = new SnapshotSeparatorVersionToken();

        assertFalse(t.isDot());
        assertTrue(t.isSnapshotSeparator());
        assertTrue(t.isSeparator());
        assertFalse(t.isNumericComponent());
        assertEquals(SnapshotSeparatorVersionToken.LITERAL, t.getLiteral());
        assertNull(t.getNumericValue());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected SnapshotSeparatorVersionToken getVersionTokenToTest() {

        return new SnapshotSeparatorVersionToken();
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
