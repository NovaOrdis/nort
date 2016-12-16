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

import io.novaordis.utilities.NotYetImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of static version utilities.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 12/16/16
 */
public class VersionUtil {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(VersionUtil.class);

    // Static ----------------------------------------------------------------------------------------------------------


    /**
     * A collection of heuristics for extracting version information from various commands output.
     */
    public static Version fromCommandStdout(String multiLineString) throws VersionFormatException {

        //
        // attempt various heuristics
        //

        Version version = null;

        try {

            return fromCanonicalString(multiLineString);
        }
        catch (VersionFormatException e) {

            //
            // no match
            //

            log.debug(e.getMessage());
        }

        throw new VersionFormatException("");
    }

    /**
     * Attempts to trim the given string and directly convert it into a Version instance.
     *
     * @throws VersionFormatException if the string cannot be converted into a version instance.
     * @throws IllegalArgumentException on null argument
     */
    public static Version fromCanonicalString(String s) throws VersionFormatException {

        if (s == null) {

            throw new IllegalArgumentException("null argument");
        }

        String ts = s.trim();

        return new Version(ts);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private VersionUtil() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
