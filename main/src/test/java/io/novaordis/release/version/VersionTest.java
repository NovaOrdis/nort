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

import io.novaordis.release.ReleaseMode;
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
public class VersionTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(VersionTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        Version v = new Version("1.2.3");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(2, v.getMinor().intValue());
        assertEquals(3, v.getPatch().intValue());
        assertNull(v.getSnapshot());

        assertEquals("1.2.3", v.getLiteral());
    }

    @Test
    public void constructor_Snapshot() throws Exception {

        Version v = new Version("1.0.2-SNAPSHOT-3");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(0, v.getMinor().intValue());
        assertEquals(2, v.getPatch().intValue());
        assertEquals(3, v.getSnapshot().intValue());

        assertEquals("1.0.2-SNAPSHOT-3", v.getLiteral());
    }

    // constructor, edge cases -----------------------------------------------------------------------------------------

    @Test
    public void constructor_Trim() throws Exception {

        Version v = new Version(" 1.2.3 ");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(2, v.getMinor().intValue());
        assertEquals(3, v.getPatch().intValue());
        assertNull(v.getSnapshot());

        assertEquals("1.2.3", v.getLiteral());
    }

    @Test
    public void constructor_OnlyMajor() throws Exception {

        Version v = new Version("1");

        assertEquals(1, v.getMajor().intValue());
        assertNull(v.getMinor());
        assertNull(v.getPatch());
        assertNull(v.getSnapshot());

        assertEquals("1", v.getLiteral());
    }

    @Test
    public void constructor_NegativeMajor() throws Exception {

        try {
            new Version("-1");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("incomplete snapshot separator", msg);
        }
    }

    @Test
    public void constructor_ZeroMajor() throws Exception {

        Version v = new Version("0");

        assertEquals(0, v.getMajor().intValue());
        assertNull(v.getMinor());
        assertNull(v.getPatch());
        assertNull(v.getSnapshot());

        assertEquals("0", v.getLiteral());
    }

    @Test
    public void constructor_MajorNotAPositiveInteger() throws Exception {

        try {
            new Version("a");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("invalid numeric version component \"a\"", msg);
        }
    }

    @Test
    public void constructor_MajorAndMinor() throws Exception {

        Version v = new Version("1.2");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(2, v.getMinor().intValue());
        assertNull(v.getPatch());
        assertNull(v.getSnapshot());

        assertEquals("1.2", v.getLiteral());
    }

    @Test
    public void constructor_MajorAndSnapshot() throws Exception {

        Version v = new Version("1-SNAPSHOT-5");

        assertEquals(1, v.getMajor().intValue());
        assertNull(v.getMinor());
        assertNull(v.getPatch());
        assertEquals(5, v.getSnapshot().intValue());

        assertEquals("1-SNAPSHOT-5", v.getLiteral());
    }

    @Test
    public void constructor_MajorAndTwoSuccessiveSeparators() throws Exception {

        try {
            new Version("1..2");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("empty version component", msg);
        }
    }

    @Test
    public void constructor_NegativeMinor() throws Exception {

        try {
            new Version("1.-1");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("incomplete snapshot separator", msg);
        }
    }

    @Test
    public void constructor_ZeroMinor() throws Exception {

        Version v = new Version("1.0");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(0, v.getMinor().intValue());
        assertNull(v.getPatch());
        assertNull(v.getSnapshot());

        assertEquals("1.0", v.getLiteral());
    }

    @Test
    public void constructor_MinorNotAPositiveInteger() throws Exception {

        try {
            new Version("1.a");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("invalid numeric version component \"a\"", msg);
        }
    }

    @Test
    public void constructor_MajorMinorAndPatch() throws Exception {

        Version v = new Version("1.2.3");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(2, v.getMinor().intValue());
        assertEquals(3, v.getPatch().intValue());
        assertNull(v.getSnapshot());

        assertEquals("1.2.3", v.getLiteral());
    }

    @Test
    public void constructor_NegativePatch() throws Exception {

        try {
            new Version("1.2.-3");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("incomplete snapshot separator", msg);
        }
    }

    @Test
    public void constructor_ZeroPatch() throws Exception {

        Version v = new Version("1.2.0");

        assertEquals(1, v.getMajor().intValue());
        assertEquals(2, v.getMinor().intValue());
        assertEquals(0, v.getPatch().intValue());
        assertNull(v.getSnapshot());

        assertEquals("1.2.0", v.getLiteral());
    }

    @Test
    public void constructor_PatchNotAPositiveInteger() throws Exception {

        try {
            new Version("1.2.a");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("invalid numeric version component \"a\"", msg);
        }
    }

    @Test
    public void constructor_NoValidSnapshotLabel() throws Exception {

        try {
            new Version("1.2.3-blah");
            fail("should have thrown exception");
        }
        catch(VersionFormatException e) {

            String msg = e.getMessage();
            assertEquals("incomplete snapshot separator", msg);
        }
    }

    // component constructor -------------------------------------------------------------------------------------------

    @Test
    public void component_constructor() throws Exception {

        Version version = new Version(1, 2, 3, 4);
        assertEquals(1, version.getMajor().intValue());
        assertEquals(2, version.getMinor().intValue());
        assertEquals(3, version.getPatch().intValue());
        assertEquals(4, version.getSnapshot().intValue());

        assertEquals("1.2.3-SNAPSHOT-4", version.getLiteral());
    }

    @Test
    public void component_constructor_NullMajor() throws Exception {

        try {
            new Version(null, 2, 3, 4);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("invalid null major component", msg);
        }
    }

    @Test
    public void component_constructor_NegativeMajor() throws Exception {

        try {
            new Version(-1, 2, 3, 4);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("invalid negative major component", msg);
        }
    }

    @Test
    public void component_constructor_NegativeMinor() throws Exception {

        try {
            new Version(1, -2, 3, 4);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("invalid negative minor component", msg);
        }
    }

    @Test
    public void component_constructor_NegativePatch() throws Exception {

        try {
            new Version(1, 2, -3, 4);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("invalid negative patch component", msg);
        }
    }

    @Test
    public void component_constructor_NegativeSnapshot() throws Exception {

        try {
            new Version(1, 2, 3, -4);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            assertEquals("invalid negative snapshot component", msg);
        }
    }

    @Test
    public void component_constructor_AllNulls() throws Exception {

        Version version = new Version(1, null, null, null);

        assertEquals(1, version.getMajor().intValue());
        assertNull(version.getMinor());
        assertNull(version.getPatch());
        assertNull(version.getSnapshot());

        assertEquals("1", version.getLiteral());
    }

    // nextVersion -----------------------------------------------------------------------------------------------------

    @Test
    public void nextVersion_major_PreviouslySnapshotOfAMajor() throws Exception {

        Version v = new Version("3-SNAPSHOT-1");

        Version v2 = Version.nextVersion(v, ReleaseMode.major);

        //
        // stay on the same major version
        //
        assertEquals("3.0", v2.getLiteral());
    }

    @Test
    public void nextVersion_major_PreviouslySnapshotOfNotAMajor() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-1");

        Version v2 = Version.nextVersion(v, ReleaseMode.major);

        //
        // go to the next major version
        //

        assertEquals("2.0", v2.getLiteral());
    }

    @Test
    public void nextVersion_major_PreviouslyPatch() throws Exception {

        Version v = new Version("1.2.3");

        Version v2 = Version.nextVersion(v, ReleaseMode.major);

        assertEquals("2.0", v2.getLiteral());
    }

    @Test
    public void nextVersion_patch() throws Exception {

        Version v = new Version("1.2.3");

        Version v2 = Version.nextVersion(v, ReleaseMode.patch);

        assertEquals("1.2.4", v2.getLiteral());
    }

    @Test
    public void nextVersion_patch_PreviouslySnapshot() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-4");

        Version v2 = Version.nextVersion(v, ReleaseMode.patch);

        assertEquals("1.2.3", v2.getLiteral());
    }

    @Test
    public void nextVersion_snapshot_NoPreviousSnapshot_NoPreviousPatch() throws Exception {

        Version v = new Version("1.2");

        Version v2 = Version.nextVersion(v, ReleaseMode.snapshot);

        assertEquals(new Version("1.2.1-SNAPSHOT-1"), v2);
    }

    @Test
    public void nextVersion_snapshot_NoPreviousSnapshot_NoPreviousPatch2() throws Exception {

        Version v = new Version("1");

        Version v2 = Version.nextVersion(v, ReleaseMode.snapshot);

        assertEquals(new Version("1.0.1-SNAPSHOT-1"), v2);
    }

    @Test
    public void nextVersion_snapshot_NoPreviousSnapshot() throws Exception {

        Version v = new Version("1.2.3");

        Version v2 = Version.nextVersion(v, ReleaseMode.snapshot);

        assertEquals(new Version("1.2.4-SNAPSHOT-1"), v2);
    }

    @Test
    public void nextVersion_snapshot_PreviousSnapshot() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-1");

        Version v2 = Version.nextVersion(v, ReleaseMode.snapshot);

        assertEquals("1.2.3-SNAPSHOT-2", v2.getLiteral());
    }

    @Test
    public void nextVersion_snapshot_PreviousSnapshot_MajorOnly() throws Exception {

        Version v = new Version("1-SNAPSHOT-1");

        Version v2 = Version.nextVersion(v, ReleaseMode.snapshot);

        assertEquals("1-SNAPSHOT-2", v2.getLiteral());
    }

    @Test
    public void nextVersion_Custom() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-15");

        ReleaseMode.custom.setCustomLabel("1.3");

        Version v2 = Version.nextVersion(v, ReleaseMode.custom);

        assertEquals("1.3", v2.getLiteral());
    }

    @Test
    public void nextVersion_CustomThatPrecedesCurrent() throws Exception {

        Version v = new Version("1.2.3");

        try {

            ReleaseMode.custom.setCustomLabel("1.2.2");
            Version.nextVersion(v, ReleaseMode.custom);
            fail("should throw exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("1.2.3 cannot be changed to preceding 1.2.2", msg);
        }
    }

    @Test
    public void nextVersion_Minor_From_Snapshot() throws Exception {

        Version v = new Version("1.2.10-SNAPSHOT-1");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.3"), nv);
    }

    @Test
    public void nextVersion_Minor_From_Patch() throws Exception {

        Version v = new Version("1.2.10");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.3"), nv);
    }

    @Test
    public void nextVersion_Minor_From_Patch_Zero() throws Exception {

        Version v = new Version("1.2.0");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.3"), nv);
    }

    @Test
    public void nextVersion_Minor_From_Minor() throws Exception {

        Version v = new Version("1.2");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.3"), nv);
    }

    @Test
    public void nextVersion_Minor_From_Major_Missing() throws Exception {

        Version v = new Version("1");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.1"), nv);
    }

    @Test
    public void nextVersion_Minor_From_Major_Zero() throws Exception {

        Version v = new Version("1.0");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.1"), nv);
    }

    @Test
    public void nextVersion_Minor_From_Major() throws Exception {

        Version v = new Version("1.2");
        Version nv = Version.nextVersion(v, ReleaseMode.minor);
        assertEquals(new Version("1.3"), nv);
    }

    // isSnapshot() ----------------------------------------------------------------------------------------------------

    @Test
    public void isSnapshot() throws Exception {

        Version v = new Version("1-SNAPSHOT-5");
        assertTrue(v.isSnapshot());
    }

    @Test
    public void isNotSnapshot() throws Exception {

        Version v = new Version("1.2");
        assertFalse(v.isSnapshot());
    }

    // isDot() ---------------------------------------------------------------------------------------------------------

    @Test
    public void isDot() throws Exception {

        Version v = new Version("1.0.1");
        assertTrue(v.isDot());
    }

    @Test
    public void isNotDot() throws Exception {

        Version v = new Version("1.2-SNAPSHOT-3");
        assertFalse(v.isDot());
    }

    // getLiteral() ----------------------------------------------------------------------------------------------------

    @Test
    public void getLiteral() throws Exception {

        Version v = new Version(1, null, null, 1);

        String s = v.getLiteral();

        assertEquals("1-SNAPSHOT-1", s);
    }

    // isMajor() -------------------------------------------------------------------------------------------------------

    @Test
    public void isMajor() throws Exception {

        Version v = new Version("1");
        assertTrue(v.isMajor());
    }

    @Test
    public void isMajor2() throws Exception {

        Version v = new Version("1.0");
        assertTrue(v.isMajor());
    }

    @Test
    public void isMajor3() throws Exception {

        Version v = new Version("1.0.0");
        assertTrue(v.isMajor());
    }

    @Test
    public void isPatchOfAMajor() throws Exception {

        Version v = new Version("1-SNAPSHOT-1");
        assertFalse(v.isMajor());
        assertTrue(v.isSnapshot());
    }

    @Test
    public void isPatch0OfAMajor() throws Exception {

        Version v = new Version("1-SNAPSHOT-0");
        assertFalse(v.isMajor());
        assertTrue(v.isSnapshot());
    }

    @Test
    public void isPatch0OfAMajor2() throws Exception {

        Version v = new Version("1.0-SNAPSHOT-0");
        assertFalse(v.isMajor());
        assertTrue(v.isSnapshot());
    }

    @Test
    public void isPatch0OfAMajor3() throws Exception {

        Version v = new Version("1.0.0-SNAPSHOT-0");
        assertFalse(v.isMajor());
        assertTrue(v.isSnapshot());
    }

    // comparable ------------------------------------------------------------------------------------------------------

    @Test
    public void compareToNull() throws Exception {

        try {

            new Version(null);
            fail("should have thrown exception");

        }
        catch(NullPointerException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void compareToSelf() throws Exception {

        Version v = new Version("1.0.0");
        assertEquals(0, v.compareTo(v));
    }

    @Test
    public void compare_MyMajorIsSmaller() throws Exception {

        Version v = new Version("1.100.100");
        Version that = new Version("2");
        assertTrue(v.compareTo(that) < 0);
    }

    @Test
    public void compare_MyMajorIsLarger() throws Exception {

        Version v = new Version("2");
        Version that = new Version("1.1000.1000");
        assertTrue(v.compareTo(that) > 0);
    }

    @Test
    public void compare_SameMajors() throws Exception {

        Version v = new Version("1");
        Version that = new Version("1");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors2() throws Exception {

        Version v = new Version("1.0");
        Version that = new Version("1");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors3() throws Exception {

        Version v = new Version("1.0.0");
        Version that = new Version("1");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors4() throws Exception {

        Version v = new Version("1.0.0");
        Version that = new Version("1.0");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors_MyMinorIsSmaller() throws Exception {

        Version v = new Version("1.1.1000");
        Version that = new Version("1.2");
        assertTrue(v.compareTo(that) < 0);
    }

    @Test
    public void compare_SameMajors_MyMinorIsLarger() throws Exception {

        Version v = new Version("1.2");
        Version that = new Version("1.1.1000");
        assertTrue(v.compareTo(that) > 0);
    }

    @Test
    public void compare_SameMajors_SameMinors() throws Exception {

        Version v = new Version("1.2");
        Version that = new Version("1.2");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors_SameMinors2() throws Exception {

        Version v = new Version("1.2.0");
        Version that = new Version("1.2");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors_SameMinors3() throws Exception {

        Version v = new Version("1.2.0");
        Version that = new Version("1.2.0");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors_SameMinors_MyPatchIsSmaller() throws Exception {

        Version v = new Version("1.2.3");
        Version that = new Version("1.2.4");
        assertTrue(v.compareTo(that) < 0);
    }

    @Test
    public void compare_SameMajors_SameMinors_MyPatchIsLarger() throws Exception {

        Version v = new Version("1.2.3");
        Version that = new Version("1.2.2");
        assertTrue(v.compareTo(that) > 0);
    }

    @Test
    public void compare_SameMajors_SameMinors_SamePatch() throws Exception {

        Version v = new Version("1.2.3");
        Version that = new Version("1.2.3");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_SameMajors_SameMinors_SamePatch_MySnapshotIsSmaller() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-4");
        Version that = new Version("1.2.3-SNAPSHOT-5");
        assertTrue(v.compareTo(that) < 0);
    }

    @Test
    public void compare_SameMajors_SameMinors_SamePatch_MySnapshotIsLarger() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-4");
        Version that = new Version("1.2.3-SNAPSHOT-3");
        assertTrue(v.compareTo(that) > 0);
    }

    @Test
    public void compare_SameMajors_SameMinors_SamePatch_SameSnapshot() throws Exception {

        Version v = new Version("1.2.3-SNAPSHOT-4");
        Version that = new Version("1.2.3-SNAPSHOT-4");
        assertTrue(v.compareTo(that) == 0);
    }

    @Test
    public void compare_TheSnapshotIsOlderThanTheCorrespondingDotVersion() throws Exception {

        Version snapshot = new Version("1.2.3-SNAPSHOT-4");
        Version dot = new Version("1.2.3");

        assertTrue(snapshot.compareTo(dot) < 0);
        assertTrue(dot.compareTo(snapshot) > 0);
    }

    // equals() and hashCode() -----------------------------------------------------------------------------------------

    @Test
    public void notEquals() throws Exception {

        Version v = new Version("1");
        Version v2 = new Version("2");
        assertFalse(v.equals(v2));
        assertTrue(v.hashCode() != v2.hashCode());
    }

    @Test
    public void equals() throws Exception {

        assertTrue(new Version("1").equals(new Version("1")));
    }

    @Test
    public void notEquals2() throws Exception {

        Version v = new Version("1.1");
        Version v2 = new Version("1.2");
        assertFalse(v.equals(v2));
        assertTrue(v.hashCode() != v2.hashCode());
    }

    @Test
    public void equals2() throws Exception {

        assertTrue(new Version("1").equals(new Version("1.0")));
    }

    @Test
    public void equals3() throws Exception {

        assertTrue(new Version("1").equals(new Version("1.0.0")));
    }

    @Test
    public void equals4() throws Exception {

        assertTrue(new Version("1.0").equals(new Version("1.0.0")));
    }

    @Test
    public void equals5() throws Exception {

        assertTrue(new Version("1.1.1").equals(new Version("1.1.1")));
    }

    @Test
    public void notEquals3() throws Exception {

        Version v = new Version("1.1.1");
        Version v2 = new Version("1.1.2");
        assertFalse(v.equals(v2));
        assertTrue(v.hashCode() != v2.hashCode());
    }

    @Test
    public void equals6() throws Exception {

        assertTrue(new Version("1.1").equals(new Version("1.1.0")));
    }

    @Test
    public void equals7() throws Exception {

        assertTrue(new Version("1.1.1-SNAPSHOT-1").equals(new Version("1.1.1-SNAPSHOT-1")));
    }

    @Test
    public void notEquals4() throws Exception {

        Version v = new Version("1.1.1-SNAPSHOT-1");
        Version v2 = new Version("1.1.1-SNAPSHOT-2");
        assertFalse(v.equals(v2));
        assertTrue(v.hashCode() != v2.hashCode());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
