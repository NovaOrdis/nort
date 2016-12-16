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

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class VersionTokenizerTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(VersionTokenTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void nullString() throws Exception {

        try {
            new VersionTokenizer(null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            log.info(e.getMessage());
        }
    }

    @Test
    public void emptyString() throws Exception {

        VersionTokenizer t = new VersionTokenizer("");

        assertFalse(t.hasNext());

        try {
            t.next();
            fail("should have thrown exception");
        }
        catch(NoSuchElementException e) {

            log.info("" + e);
        }
    }

    @Test
    public void blank() throws Exception {

        VersionTokenizer t = new VersionTokenizer("  ");

        assertFalse(t.hasNext());

        try {
            t.next();
            fail("should have thrown exception");
        }
        catch(NoSuchElementException e) {

            log.info("" + e);
        }
    }

    @Test
    public void dots() throws Exception {

        VersionTokenizer t = new VersionTokenizer("...");

        VersionToken tok;

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isDot());

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isDot());

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isDot());

        assertFalse(t.hasNext());
    }

    @Test
    public void incompleteSnapshotSeparator() throws Exception {

        VersionTokenizer t = new VersionTokenizer("-");

        assertTrue(t.hasNext());

        try {
            t.next();
            fail("should throw exception");
        }
        catch(VersionFormatException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("incomplete snapshot separator", msg);
        }
    }

    @Test
    public void incompleteSnapshotSeparator2() throws Exception {

        VersionTokenizer t = new VersionTokenizer("-SNAPSHOT");

        assertTrue(t.hasNext());

        try {
            t.next();
            fail("should throw exception");
        }
        catch(VersionFormatException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("incomplete snapshot separator", msg);
        }
    }

    @Test
    public void SnapshotSeparator() throws Exception {

        VersionTokenizer t = new VersionTokenizer("-SNAPSHOT-");

        assertTrue(t.hasNext());
        VersionToken tok = t.next();
        assertTrue(tok.isSnapshotSeparator());

        assertFalse(t.hasNext());
    }

    @Test
    public void invalidNumericComponent() throws Exception {

        VersionTokenizer t = new VersionTokenizer("a");

        assertTrue(t.hasNext());

        try {
            t.next();
            fail("should throw exception");
        }
        catch(VersionFormatException e) {
            String msg = e.getMessage();
            log.info(msg);
            assertEquals("invalid numeric version component \"a\"", msg);
        }
    }

    @Test
    public void valid_Numeric() throws Exception {

        VersionTokenizer t = new VersionTokenizer("1");

        assertTrue(t.hasNext());

        VersionToken tok = t.next();

        assertTrue(tok.isNumericComponent());
        assertEquals(1, tok.getNumericValue().intValue());
        assertEquals("1", tok.getLiteral());

        assertFalse(t.hasNext());
    }

    @Test
    public void valid_NumericDotNumeric() throws Exception {

        VersionTokenizer t = new VersionTokenizer("1.2");

        VersionToken tok;

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isNumericComponent());
        assertEquals(1, tok.getNumericValue().intValue());

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isDot());

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isNumericComponent());
        assertEquals(2, tok.getNumericValue().intValue());

        assertFalse(t.hasNext());
    }

    @Test
    public void valid_NumericSnapshotNumeric() throws Exception {

        VersionTokenizer t = new VersionTokenizer("1-SNAPSHOT-2");

        VersionToken tok;

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isNumericComponent());
        assertEquals(1, tok.getNumericValue().intValue());

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isSnapshotSeparator());

        assertTrue(t.hasNext());
        tok = t.next();
        assertTrue(tok.isNumericComponent());
        assertEquals(2, tok.getNumericValue().intValue());

        assertFalse(t.hasNext());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
