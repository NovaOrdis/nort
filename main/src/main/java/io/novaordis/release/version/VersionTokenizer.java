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

import java.util.NoSuchElementException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class VersionTokenizer {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private int crt;
    private String s;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param s the version literal to be tokenized
     */
    public VersionTokenizer(String s) {

        if (s == null) {
            throw new IllegalArgumentException("null version string");
        }

        this.s = s.trim();
        crt = 0;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public boolean hasNext() {

        return crt < s.length();
    }


    /**
     * @throws NoSuchElementException if there are no more tokens to return
     */
    public VersionToken next() throws NoSuchElementException, VersionFormatException {

        if (crt >= s.length()) {
            throw new NoSuchElementException();
        }

        int next;

        for(next = crt; next < s.length(); next ++) {

            if (s.charAt(next) == '.') {

                //
                // dot separator identified
                //

                //
                // if we're the first in line, return ourselves ...
                //

                if (crt == next) {

                    crt++;
                    return new DotVersionToken();
                }
                else {

                    //
                    // ... otherwise return the content already identified
                    //
                    break;
                }
            }
            else if (s.charAt(next) == '-') {

                //
                // dash identified, if we're the first in line, return ourselves ...
                //

                if (crt == next) {

                    //
                    // the only possibility is to have a full snapshot separator
                    //

                    if (s.length() - crt >= SnapshotSeparatorVersionToken.LITERAL.length()) {

                        crt += SnapshotSeparatorVersionToken.LITERAL.length();
                        return new SnapshotSeparatorVersionToken();
                    }

                    throw new VersionFormatException("incomplete snapshot separator");
                }
                else {

                    //
                    // ... otherwise return the content already identified
                    //
                    break;
                }
            }

            //
            // advance the 'next' cursor until we encounter the next separator
            //
        }

        String literal = s.substring(crt, next);

        crt = next;

        //
        // we only accept numeric components for the time being
        //

        return new NumericVersionToken(literal);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
