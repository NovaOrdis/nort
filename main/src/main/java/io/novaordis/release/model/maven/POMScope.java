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
import io.novaordis.utilities.expressions.ScopeImpl;
import io.novaordis.utilities.expressions.Variable;
import io.novaordis.utilities.xml.editor.InLineXMLEditor;
import io.novaordis.utilities.xml.editor.XMLElement;

import java.util.List;

/**
 * A variable scope associated with a POM. It does not maintain local variables but instead it reads (and in some
 * cases caches) them from the POM and from its parents. We cannot use it to set property values - Variable.set() will
 * will throw UnsupportedOperationException.
 *
 * TODO: this is a quick "bolt-on" solution after the introduction of the generic variable and expressions system
 * https://kb.novaordis.com/index.php/Nova_Ordis_Generic_Variable_and_Expression_System. A better implementation can be
 * written, by taking advantage of variable and expression features introduced by the generic variable and expressions
 * system.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/27/16
 */
public class POMScope extends ScopeImpl {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // the associated POM. Through that instance it has access to the higher level of the hierarchy
    //
    private POM pom;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * This constructor DOES not establishes the scope hierarchy. The caller must do it.
     *
     * It is important to do it AFTER the constructor completes, to give local properties to be declared correctly.
     *
     * @param pom can never be null.
     */
    public POMScope(POM pom, InLineXMLEditor pomEditor) {

        if (pom == null) {

            throw new IllegalArgumentException("null pom");
        }

        if (pomEditor == null) {

            throw new IllegalArgumentException("null in-line XML editor");
        }

        this.pom = pom;

        //
        // initialize "known" properties first
        //

        //
        // "version", "project.version"
        //

        try {

            Version v = pom.getVersion();

            String s = v.getLiteral();

            declare("version", s);
            declare("project.version", s);
        }
        catch(VersionFormatException e) {

            throw new IllegalStateException(e);
        }

        //
        // initialize local variables in scope with name/values corresponding to the properties declared in the
        // associated XML file
        //

        List<XMLElement> propertyElements = pomEditor.getElements("/project/properties");

        if (!propertyElements.isEmpty()) {

            for(XMLElement p: propertyElements) {

                declare(p.getName(), p.getValue());
            }
        }
    }

    // Scope overrides -------------------------------------------------------------------------------------------------

    @Override
    public Variable getVariable(String name) {

        //
        // wrap the underling instance in our own implementation
        //

        Variable v = super.getVariable(name);

        if (v == null) {

            return null;
        }

        return new POMVariable(v);
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
