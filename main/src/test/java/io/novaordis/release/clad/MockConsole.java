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

package io.novaordis.release.clad;

import io.novaordis.clad.application.Console;
import io.novaordis.utilities.NotYetImplementedException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/19/17
 */
public class MockConsole implements Console {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String warningContent;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Console implementation ------------------------------------------------------------------------------------------

    @Override
    public void info(String s) {
        throw new NotYetImplementedException("info() NOT YET IMPLEMENTED");
    }

    @Override
    public void warn(String s) {

        if (warningContent == null) {

            warningContent = s;
        }
        else {

            warningContent += s;
        }
    }

    @Override
    public void error(String s) {
        throw new NotYetImplementedException("error() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getWarningContent() {

        return warningContent;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
