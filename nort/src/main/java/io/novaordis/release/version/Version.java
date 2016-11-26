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

/**
 * Correctly implements equals() and hashCode()
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class Version implements Comparable<Version> {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Computes the next logical version that should succeed the given version, for the given release mode. If it is
     * a major, minor, patch or snapshot release, the version is incremented. If it is a custom release, the custom
     * label is used (but only if the custom label succeeds the current version).
     *
     * @return a Version instance corresponding to the next logical version. This Version instance passed as argument
     * is not modified.
     *
     * @exception IllegalArgumentException if we attempt to change to a custom version that precedes the current
     * version.
     */
    public static Version nextVersion(Version current, ReleaseMode releaseMode) throws IllegalArgumentException {

        Integer major = current.getMajor();
        Integer minor = current.getMinor();
        Integer patch = current.getPatch();
        Integer snapshot = current.getSnapshot();

        if (ReleaseMode.major.equals(releaseMode)) {

            if (current.isSnapshot() &&
                    (major != null && (minor == null || minor == 0) && (patch == null || patch == 0))) {

                //
                // we're the snapshot of a major, stay on the same major version
                //

                return new Version(current.getMajor(), 0, null, null);
            }
            else {

                // snapshot of something else than a major or dot - go to the next major
                return new Version(current.getMajor() + 1, 0, null, null);
            }
        }
        else if (ReleaseMode.minor.equals(releaseMode)) {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }
        else if (ReleaseMode.patch.equals(releaseMode)) {

            if (current.isSnapshot()) {

                //
                // we're a snapshot, stay on the same patch version, without the snapshot
                //

                snapshot = null;
            }
            else {

                //
                // we're a dot, increment the patch number
                //

                if (patch == null) {

                    patch = 1;
                }
                else {

                    patch += 1;
                }
            }
        }
        else if (ReleaseMode.snapshot.equals(releaseMode)) {

            //
            // if we're already a snapshot, increment the snapshot component, otherwise increment the patch and set
            // the snapshot to 1
            //

            if (snapshot != null) {

                snapshot += 1;
            }
            else {

                snapshot = 1;

                if (patch == null) {
                    patch = 1;
                }
                else {
                    patch += 1;
                }
            }
        }
        else if (ReleaseMode.custom.equals(releaseMode)) {

            //
            // verify that we're not setting to a preceding version
            //

            Version c = releaseMode.getCustomVersion();

            if (current.compareTo(c) > 0) {
                throw new IllegalArgumentException(current + " cannot be changed to preceding " + c);
            }

            return c;
        }
        else {
            throw new IllegalArgumentException(releaseMode + " not a valid increment mode");
        }

        return new Version(major, minor, patch, snapshot);
    }

    // Package protected static ----------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;
    private Integer major;
    private Integer minor;
    private Integer patch;
    private Integer snapshot;

    private VersionToken last;

    // Constructors ----------------------------------------------------------------------------------------------------

    public Version(String literal) throws VersionFormatException {

        parse(literal);
    }

    /**
     *
     * @param major cannot be null
     * @param minor may be null
     * @param patch may be null
     * @param snapshot may be null
     * @exception IllegalArgumentException on invalid component values.
     */
    public Version(Integer major, Integer minor, Integer patch, Integer snapshot) throws IllegalArgumentException {

        if (major == null) {
            throw new IllegalArgumentException("invalid null major component");
        }

        if (major < 0) {
            throw new IllegalArgumentException("invalid negative major component");
        }

        this.major = major;

        if (minor != null && minor < 0) {
            throw new IllegalArgumentException("invalid negative minor component");
        }

        this.minor = minor;

        if (patch != null && patch < 0) {
            throw new IllegalArgumentException("invalid negative patch component");
        }

        this.patch = patch;

        if (snapshot != null && snapshot < 0) {
            throw new IllegalArgumentException("invalid negative snapshot component");
        }

        this.snapshot = snapshot;
    }

    // Comparable implementation ---------------------------------------------------------------------------------------

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Version o) {

        if (o == null) {
            throw new NullPointerException("null version"); // as per javadoc
        }

        if (this == o) {
            return 0;
        }

        // negative if this version is less than o

        Integer thatMajor = o.major;
        Integer thatMinor = o.minor;
        Integer thatPatch = o.patch;
        Integer thatSnapshot = o.snapshot;

        int diff = major - thatMajor;

        if (diff != 0) {

            return diff;

        }

        //
        // same majors
        //

        diff = (minor == null ? 0 : minor) - (thatMinor == null ? 0 : thatMinor);

        if (diff != 0) {

            return diff;
        }

        //
        // same minors
        //

        diff = (patch == null ? 0 : patch) - (thatPatch == null ? 0 : thatPatch);

        if (diff != 0) {

            return diff;
        }

        //
        // same patches
        //


        diff = (snapshot == null ? 0 : snapshot) - (thatSnapshot == null ? 0 : thatSnapshot);

        return diff;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Cannot return null.
     */
    public Integer getMajor() {

        return major;
    }

    /**
     * May return null if there is no minor version information.
     */
    public Integer getMinor() {

        return minor;
    }

    /**
     * May return null if there is no patch information.
     */
    public Integer getPatch() {

        return patch;
    }

    /**
     * May return null if there is no snapshot information.
     */
    public Integer getSnapshot() {

        return snapshot;
    }

    /**
     * @return the string representation of the version.
     */
    public String getLiteral() {

        if (literal != null) {

            return literal;
        }

        literal = "";

        if (snapshot != null) {

            literal = "-SNAPSHOT-" + snapshot;
        }

        if (patch != null) {

            literal = patch + literal;
        }

        if (minor != null) {

            literal = minor + (literal.length() > 0 ? "." : "") + literal;
        }

        literal = major + (literal.length() > 0 ? ( literal.startsWith("-") ? "" : ".") : "") + literal;

        return literal;
    }

    public boolean isSnapshot() {

        return getSnapshot() != null;
    }

    public boolean isDot() {

        return getSnapshot() == null;
    }

    public boolean isMajor() {

        return
                major != null &&
                        (minor == null || minor == 0) &&
                        (patch == null || patch == 0) &&
                        snapshot == null;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Version)) {
            return false;
        }

        Version that = (Version)o;

        if (!major.equals(that.major)) {
            return false;
        }

        int minorAsInt = minor == null ? 0 : minor;
        int thatMinorAsInt = that.minor == null ? 0 : that.minor;

        if (minorAsInt != thatMinorAsInt) {
            return false;
        }

        int patchAsInt = patch == null ? 0 : patch;
        int thatPatchAsInt = that.patch == null ? 0 : that.patch;

        if (patchAsInt != thatPatchAsInt) {
            return false;
        }

        int snapshotAsInt = snapshot == null ? 0 : snapshot;
        int thatSnapshotAsInt = that.snapshot == null ? 0 : that.snapshot;

        return snapshotAsInt == thatSnapshotAsInt;

    }

    @Override
    public int hashCode() {

        return
                7 +
                        major * 11 +
                        (minor == null ? 0 : minor) * 13 +
                        (patch == null ? 0 : patch) * 17 +
                        (snapshot == null ? 0 : snapshot) * 19;
    }

    @Override
    public String toString() {

        return getLiteral();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void parse(String origLiteral) throws VersionFormatException {

        literal = origLiteral.trim();

        VersionTokenizer t = new VersionTokenizer(literal);
        VersionComponentType nextComponentType = VersionComponentType.major;

        while(t.hasNext()) {

            if (nextComponentType == null) {
                //
                // we don't need more components, yet the literal has them
                //
                throw new VersionFormatException("too many version components");
            }

            VersionToken crt = t.next();
            nextComponentType = installComponent(nextComponentType, crt);
            last = crt;
        }

        if (major == null) {

            throw new VersionFormatException("\"" + literal + "\" missing the major component");
        }

        //
        // cleanup
        //
        last = null;
    }

    /**
     * @return the next component in line after this installation or null if we don't expect any more components
     */
    private VersionComponentType installComponent(VersionComponentType nextComponent, VersionToken token)
            throws VersionFormatException {

        if (token == null) {

            throw new IllegalArgumentException("null version component");
        }

        if (VersionComponentType.major.equals(nextComponent)) {

            if (major != null) {

                throw new IllegalArgumentException("the major component already identified: " + major);
            }

            if (!token.isNumericComponent()) {

                throw new VersionFormatException("expecting a numeric component and got " + token);
            }

            major = token.getNumericValue();
            return VersionComponentType.minor;
        }

        // minor, patch or snapshot

        if (VersionComponentType.minor.equals(nextComponent)) {

            if (minor != null) {

                throw new IllegalArgumentException("the minor component already identified: " + minor);
            }

            if (token.isDot()) {

                if (last != null && last.isSeparator()) {

                    throw new VersionFormatException("empty version component");
                }

                // else ignore
                return VersionComponentType.minor;
            }

            if (token.isSnapshotSeparator()) {

                return VersionComponentType.snapshot;
            }

            if (!token.isNumericComponent()) {

                throw new VersionFormatException("expecting a numeric component and got " + token);
            }

            minor = token.getNumericValue();
            return VersionComponentType.patch;
        }

        if (VersionComponentType.patch.equals(nextComponent)) {

            if (patch != null) {

                throw new IllegalArgumentException("the patch component already identified: " + patch);
            }

            if (token.isDot()) {

                if (last != null && last.isSeparator()) {

                    throw new VersionFormatException("empty version component");
                }

                // else ignore
                return VersionComponentType.patch;
            }

            if (token.isSnapshotSeparator()) {

                return VersionComponentType.snapshot;
            }

            if (!token.isNumericComponent()) {

                throw new VersionFormatException("expecting a numeric component and got " + token);
            }

            patch = token.getNumericValue();
            return VersionComponentType.snapshot;
        }

        if (VersionComponentType.snapshot.equals(nextComponent)) {

            if (token.isSnapshotSeparator()) {

                if (last != null && last.isSeparator()) {

                    throw new VersionFormatException("empty version component");
                }

                // else ignore
                return VersionComponentType.snapshot;
            }

            if (!token.isNumericComponent()) {

                throw new VersionFormatException("expecting a numeric component and got " + token);
            }

            snapshot = token.getNumericValue();
            return null;
        }

        return null;
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
