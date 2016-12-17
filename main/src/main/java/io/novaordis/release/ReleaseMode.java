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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public enum ReleaseMode {

    info,
    snapshot,
    minor,
    major,
    patch,
    custom; // custom release label

    // the release label associated with the custom release
    private Version customLabel;

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return true if this mode increments the release metadata (major/minor/patch/snapshot), false otherwise
     */
    public boolean isIncrement() {

        return this.equals(major) || this.equals(minor) || this.equals(patch) || this.equals(snapshot);
    }

    /**
     * @return true if this is a "dot release", meaning that it will set the version metadata to a "dot version".
     *
     * @{linkUrl https://kb.novaordis.com/index.php/Nova_Ordis_Release_Tools_User_Manual_-_Concepts#Dot_Version}
     */
    public boolean isDot() {

        if (this.equals(custom)) {

            return customLabel != null && !customLabel.isSnapshot();
        }

        return this.equals(major) || this.equals(minor) || this.equals(patch);
    }

    public boolean isSnapshot() {

        if (this.equals(custom)) {

            Version customVersion = getCustomVersion();
            return customVersion != null && customVersion.isSnapshot();
        }

        return this.equals(snapshot);
    }

    public boolean isCustom() {

        return this.equals(custom);
    }

    /**
     * @param s a custom version label. Can be null, in which case it resets the internal state to null.
     *
     * Only applies to custom release, will throw IllegalStateException if used on any other type of release mode.
     *
     * @return itself.
     *
     * @exception IllegalStateException if attempting to set a custom label on a non-custom release mode.
     * @exception VersionFormatException if the string does not have a valid version format
     */
    public ReleaseMode setCustomLabel(String s) throws VersionFormatException {

        if (!this.equals(custom)) {

            throw new IllegalStateException("cannot set custom label on a non-custom release mode");
        }

        if (s == null) {
            //
            // reset
            //
            this.customLabel = null;
        }
        else {

            this.customLabel = new Version(s);
        }

        return this;
    }

    /**
     * @return null on a non-custom release mode. May return null on a custom release mode if the label was not
     * previously set with setCustomLabel()
     *
     * @see ReleaseMode#setCustomLabel(String)
     */
    public Version getCustomVersion() {

        if (customLabel == null) {
            return null;
        }

        return customLabel;
    }

    /**
     * Resets the custom label. It only makes sense for "custom" release mode.
     */
    public void reset() {

        this.customLabel = null;
    }

    @Override
    public String toString() {

        String s = name();

        if (custom.equals(this)) {

            s += " " + getCustomVersion();
        }

        return s;
    }
}
