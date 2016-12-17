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

package io.novaordis.release;

import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class ReleaseModeTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ReleaseModeTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @After
    public void cleanup() {

        ReleaseMode.custom.reset();
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void isIncrement() throws Exception {

        assertFalse(ReleaseMode.info.isIncrement());
        assertTrue(ReleaseMode.major.isIncrement());
        assertTrue(ReleaseMode.minor.isIncrement());
        assertTrue(ReleaseMode.patch.isIncrement());
        assertTrue(ReleaseMode.snapshot.isIncrement());
        assertFalse(ReleaseMode.custom.isIncrement());
    }

    // isDot -----------------------------------------------------------------------------------------------------------

    @Test
    public void isDot() throws Exception {

        assertTrue(ReleaseMode.major.isDot());
        assertTrue(ReleaseMode.minor.isDot());
        assertTrue(ReleaseMode.patch.isDot());
    }

    @Test
    public void isNotDot() throws Exception {

        assertFalse(ReleaseMode.snapshot.isDot());
    }

    @Test
    public void customDot() throws Exception {

        ReleaseMode mode = ReleaseMode.custom;
        mode.setCustomLabel("1.2.3");
        assertTrue(mode.isDot());
    }

    @Test
    public void customNotDot() throws Exception {

        ReleaseMode mode = ReleaseMode.custom;
        mode.setCustomLabel("1.2.3-SNAPSHOT-1");
        assertFalse(mode.isDot());
    }

    @Test
    public void customNotDot_NoLabel() throws Exception {

        ReleaseMode mode = ReleaseMode.custom;
        ReleaseMode mode2 = mode.setCustomLabel(null);
        assertNull(mode.getCustomVersion());
        assertFalse(mode.isDot());
        assertEquals(mode, mode2);
    }

    // setCustomLabel() ------------------------------------------------------------------------------------------------

    @Test
    public void setCustomLabel_OnCustomRelease() throws Exception {

        ReleaseMode mode = ReleaseMode.custom;
        ReleaseMode mode2 = mode.setCustomLabel("1.2.3");
        //noinspection ConstantConditions
        assertEquals("1.2.3", mode.getCustomVersion().getLiteral());
        assertEquals(mode, mode2);
    }

    @Test
    public void setCustomLabel_OnNonCustomRelease() throws Exception {

        ReleaseMode mode = ReleaseMode.major;

        try {
            mode.setCustomLabel("1.2.3");
            fail("should throw exception");
        }
        catch(IllegalStateException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("cannot set custom label on a non-custom release mode", msg);
        }
    }

    @Test
    public void setCustomLabel_NotAValidVersionFormat() throws Exception {

        ReleaseMode mode = ReleaseMode.custom;

        try {
            mode.setCustomLabel("blah");
            fail("should throw exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid numeric version component \"blah\"", msg);
        }
    }

    @Test
    public void getCustomLabel_NonCustomReleaseModesAndUninitializedCustomMode() throws Exception {

        assertNull(ReleaseMode.major.getCustomVersion());
        assertNull(ReleaseMode.minor.getCustomVersion());
        assertNull(ReleaseMode.patch.getCustomVersion());
        assertNull(ReleaseMode.snapshot.getCustomVersion());
        assertNull(ReleaseMode.custom.getCustomVersion());
    }

    // reset() ---------------------------------------------------------------------------------------------------------

    @Test
    public void reset() throws Exception {

        assertNull(ReleaseMode.custom.getCustomVersion());
        ReleaseMode.custom.setCustomLabel("1.2.3");
        assertEquals(new Version("1.2.3"), ReleaseMode.custom.getCustomVersion());

        ReleaseMode.custom.reset();
        assertNull(ReleaseMode.custom.getCustomVersion());
    }

    // isSnapshot() ----------------------------------------------------------------------------------------------------

    @Test
    public void isSnapshot() throws Exception {

        assertFalse(ReleaseMode.major.isSnapshot());
        assertFalse(ReleaseMode.minor.isSnapshot());
        assertFalse(ReleaseMode.patch.isSnapshot());
        assertTrue(ReleaseMode.snapshot.isSnapshot());

        ReleaseMode.custom.setCustomLabel("1.2.3");
        assertFalse(ReleaseMode.custom.isSnapshot());

        ReleaseMode.custom.setCustomLabel("1.2.3-SNAPSHOT-1");
        assertTrue(ReleaseMode.custom.isSnapshot());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
