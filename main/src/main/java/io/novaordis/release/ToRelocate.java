/*
 * Copyright (c) 2017 Nova Ordis LLC
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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/20/17
 */
public class ToRelocate {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * TODO: the existence of this method is a sign that the VariableProvider interface must be typed.
     *
     * @return true only if string is "true" (irrespective of case). Otherwise is false, even for null.
     *
     * @exception IllegalArgumentException if s is not null, "true" or "false" (capitalization notwithstanding)
     */
    public static boolean toBoolean(String s) {

        if (s == null) {

            return false;
        }

        String lcs = s.toLowerCase();

        if ("true".equals(lcs)) {

            return true;
        }
        else if ("false".equals(lcs)) {

            return false;
        }
        else {

            throw new IllegalArgumentException("\"" + s + "\" is not a valid boolean value");
        }
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private ToRelocate() {
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
