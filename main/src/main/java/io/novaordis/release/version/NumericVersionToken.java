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

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class NumericVersionToken implements VersionToken {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String literal;
    private Integer value;

    // Constructors ----------------------------------------------------------------------------------------------------

    public NumericVersionToken(String literal) throws VersionFormatException {

        try {
            value = Integer.parseInt(literal);
        }
        catch(Exception e) {
            throw new VersionFormatException("invalid numeric version component \"" + literal + "\"");
        }

        if (value < 0) {
            throw new VersionFormatException("\"" + literal + "\" negative version component");
        }

        this.literal = literal;
    }

    // VersionToken implementation -------------------------------------------------------------------------------------

    @Override
    public boolean isDot() {

        return false;
    }

    @Override
    public boolean isSnapshotSeparator() {

        return false;
    }

    @Override
    public boolean isSeparator() {

        return false;
    }

    @Override
    public boolean isNumericComponent() {

        return true;
    }

    @Override
    public String getLiteral() {

        return literal;
    }

    @Override
    public Integer getNumericValue() {

        return value;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return getLiteral();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
