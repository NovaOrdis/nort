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

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.Option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class MockConfiguration implements Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // if get is broken, get() invocation will fail with a synthetic RuntimeException
    private boolean getBroken;

    private Map<String, String> genericLabels;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockConfiguration() {
        this.genericLabels = new HashMap<>();
    }

    // Configuration implementation ------------------------------------------------------------------------------------

    @Override
    public String getApplicationName() {
        throw new RuntimeException("getApplicationName() NOT YET IMPLEMENTED");
    }

    @Override
    public List<Option> getGlobalOptions() {
        throw new RuntimeException("getGlobalOptions() NOT YET IMPLEMENTED");
    }

    @Override
    public Option getGlobalOption(Option definition) {
        throw new RuntimeException("getGlobalOption() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isVerbose() {

        return true;
    }

    @Override
    public void set(String configurationLabel, String value) {

        genericLabels.put(configurationLabel, value);
    }

    @Override
    public String get(String configurationLabel) {

        if (getBroken) {
            throw new RuntimeException("SYNTHETIC");
        }

        return genericLabels.get(configurationLabel);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Make get() invocation to fail with a synthetic RuntimeException
     */
    public void breakGet() {

        this.getBroken = true;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
