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

import io.novaordis.utilities.NotYetImplementedException;
import io.novaordis.utilities.env.EnvironmentVariableProvider;
import io.novaordis.utilities.variable.VariableProvider;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 12/20/16
 */
public class NortVariableProvider implements VariableProvider {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private EnvironmentVariableProvider environmentVariableProvider;

    // Constructors ----------------------------------------------------------------------------------------------------

    public NortVariableProvider() {

        this.environmentVariableProvider = EnvironmentVariableProvider.getInstance();
    }

    // VariableProvider implementation ---------------------------------------------------------------------------------

    @Override
    public String getVariableValue(String s) {
        throw new NotYetImplementedException("getVariableValue() NOT YET IMPLEMENTED");
    }

    @Override
    public String setVariableValue(String s, String s1) {
        throw new NotYetImplementedException("setVariableValue() NOT YET IMPLEMENTED");
    }

    @Override
    public VariableProvider getVariableProviderParent() {
        throw new NotYetImplementedException("getVariableProviderParent() NOT YET IMPLEMENTED");
    }

    @Override
    public void setVariableProviderParent(VariableProvider variableProvider) {
        throw new NotYetImplementedException("setVariableProviderParent() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public EnvironmentVariableProvider getEnvironmentVariableProvider() {

        return environmentVariableProvider;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setEnvironmentVariableProvider(EnvironmentVariableProvider p) {

        this.environmentVariableProvider = p;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
