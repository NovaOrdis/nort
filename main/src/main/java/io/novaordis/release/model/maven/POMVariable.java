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

package io.novaordis.release.model.maven;

import io.novaordis.utilities.expressions.Variable;

/**
 * Delegates to cached or "live" variables, prevents writing.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 9/18/17
 */
public class POMVariable implements Variable {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Variable delegate;

    // Constructors ----------------------------------------------------------------------------------------------------

    public POMVariable(Variable delegate) {
        
        this.delegate = delegate;
    }

    // Variable implementation -----------------------------------------------------------------------------------------

    @Override
    public String name() {

        return delegate.name();
    }

    @Override
    public Class type() {

        return String.class;
    }

    @Override
    public Object get() {

        return delegate.get();
    }

    @Override
    public Object set(Object value) {

        throw new UnsupportedOperationException(
                "a POM variable provider is read-only, it cannot be used to set variable values");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
