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

package io.novaordis.release.model;

/**
 * The type of an artifact produced by an object.
 *
 * For more details see:
 *
 * @{linktourl https://kb.novaordis.com/index.php/Nova_Ordis_Release_Tools_User_Manual_-_Concepts#Release_Artifacts}
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/23/16
 */
public enum ArtifactType {

    JAR_LIBRARY("jar", "JAR library"),
    BINARY_DISTRIBUTION("zip", "binary distribution");

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @return the corresponding ArtifactType from the given string, which is usually the value of a pom.xml packaging
     * ("jar", for example). Will return null, if the packaging is "pom" - no publishable artifact.
     *
     * @exception IllegalArgumentException on a null String or a string that cannot be converted to an artifact type.
     */
    public static ArtifactType fromString(String s) throws IllegalArgumentException {

        if (s == null) {

            throw new IllegalArgumentException("null packaging");
        }

        //
        // "pom" is a special case, there's no corresponding artifact type
        //

        if ("pom".equals(s)) {
            return null;
        }

        for(ArtifactType t : values()) {

            if (t.getExtension().equals(s)) {
                return t;
            }
        }

        throw new IllegalArgumentException("cannot convert \"" + s + "\" to ArgumentType");
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private String extension;
    private String label;

    ArtifactType(String extension, String label) {

        this.extension = extension;
        this.label = label;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * @return the string to use in logs and other human-readable content
     */
    public String getLabel() {

        return label;
    }
}
