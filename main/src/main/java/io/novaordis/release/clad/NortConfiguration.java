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

package io.novaordis.release.clad;

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.Option;
import io.novaordis.utilities.NotYetImplementedException;

import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 12/20/16
 */
public class NortConfiguration implements Configuration {

    //
    //
    // TODO This is an experimental implementation, and upon the success of the experiment, some of the features
    //      will be ported to clad Configuration support. Currently we wrap around the native configuration instance
    //      and delegate to it
    //
    // Example:
    //
    // 1. Extensible configuration implementation, which offers extensible typed access to application-specific
    //    configuration.
    //
    // 2. Variable support
    //
    //

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Configuration delegateNativeConfiguration;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Configuration implementation ------------------------------------------------------------------------------------

    @Override
    public String getApplicationName() {
        throw new NotYetImplementedException("getApplicationName() NOT YET IMPLEMENTED");
    }

    @Override
    public List<Option> getGlobalOptions() {
        throw new NotYetImplementedException("getGlobalOptions() NOT YET IMPLEMENTED");
    }

    @Override
    public Option getGlobalOption(Option option) {
        throw new NotYetImplementedException("getGlobalOption() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isVerbose() {
        throw new NotYetImplementedException("isVerbose() NOT YET IMPLEMENTED");
    }

    @Override
    public void set(String s, String s1) {
        throw new NotYetImplementedException("set() NOT YET IMPLEMENTED");
    }

    @Override
    public String get(String s) {
        throw new NotYetImplementedException("get() NOT YET IMPLEMENTED");
    }


    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
