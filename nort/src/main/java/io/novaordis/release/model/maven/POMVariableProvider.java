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

package io.novaordis.release.model.maven;

import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;
import io.novaordis.utilities.variable.VariableProvider;
import io.novaordis.utilities.xml.editor.InLineXMLEditor;
import io.novaordis.utilities.xml.editor.XMLElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A variable provider associated to a POM. It does not maintain local variables but instead it reads (and in some
 * cases caches) them from the pom and from its parents. We cannot use it to set properties via its interface
 * (setVariableValue() will throw UnsupportedOperationException.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/27/16
 */
public class POMVariableProvider implements VariableProvider {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // the associated POM. Through that instance it has access to the higher level of the hierarchy
    //
    private POM pom;

    private Map<String, String> cachedPomProperties;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param pom can never be null
     */
    public POMVariableProvider(POM pom, InLineXMLEditor pomEditor) {

        if (pom == null) {

            throw new IllegalArgumentException("null pom");
        }

        if (pomEditor == null) {

            throw new IllegalArgumentException("null in-line XML editor");
        }


        this.pom = pom;

        //
        // populate the local variable provider with the variables corresponding to the properties declared in the
        // associated XML file
        //

        this.cachedPomProperties = new HashMap<>();

        List<XMLElement> propertyElements = pomEditor.getElements("/project/properties");

        if (!propertyElements.isEmpty()) {

            for(XMLElement p: propertyElements) {

                cachedPomProperties.put(p.getName(), p.getValue());
            }
        }
    }


    // VariableProvider implementation ---------------------------------------------------------------------------------

    @Override
    public String getVariableValue(String variableName) {

        //
        // handle "known" properties first
        //

        if ("version".equals(variableName) || "project.version".equals(variableName)) {

            try {

                Version v = pom.getVersion();
                return v.getLiteral();
            }
            catch(VersionFormatException e) {
                throw new IllegalStateException(e);
            }
        }

        //
        // try the local cached properties, if any
        //

        String variableValue = cachedPomProperties.get(variableName);

        if (variableValue != null) {

            return variableValue;
        }

        //
        // not found, delegate to parent
        //

        POM parentPom = pom.getParent();

        if (parentPom == null) {

            return null;
        }

        POMVariableProvider parent = parentPom.getVariableProvider();

        if (parent == null) {

            return null;
        }

        //noinspection UnnecessaryLocalVariable
        String v = parent.getVariableValue(variableName);
        return v;
    }

    @Override
    public String setVariableValue(String variableName, String variableValue) {

        throw new UnsupportedOperationException(
                "a POM variable provider is read-only, it cannot be used to set variable values");
    }

    @Override
    public VariableProvider getVariableProviderParent() {
        throw new RuntimeException("getVariableProviderParent() NOT YET IMPLEMENTED");
    }

    @Override
    public void setVariableProviderParent(VariableProvider parent) {
        throw new RuntimeException("setVariableProviderParent() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public POM getPom() {

        return pom;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
