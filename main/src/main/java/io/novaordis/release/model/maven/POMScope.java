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
import io.novaordis.utilities.expressions.EncloseableScope;
import io.novaordis.utilities.expressions.Scope;
import io.novaordis.utilities.expressions.UndeclaredVariableException;
import io.novaordis.utilities.expressions.Variable;
import io.novaordis.utilities.expressions.VariableReferenceResolver;
import io.novaordis.utilities.xml.editor.InLineXMLEditor;
import io.novaordis.utilities.xml.editor.XMLElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A variable scope associated with a POM. It does not maintain local variables but instead it reads them in-line from
 * the POM and from its parents. We cannot use it to set property values - Variable.set() will will throw
 * UnsupportedOperationException.
 *
 * TODO: this is a quick "bolt-on" solution after the introduction of the generic variable and expressions system
 * https://kb.novaordis.com/index.php/Nova_Ordis_Generic_Variable_and_Expression_System. A better implementation can be
 * written, by taking advantage of variable and expression features introduced by the generic variable and expressions
 * system. Move Maven support to novaordis-utilities.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/27/16
 */
public class POMScope implements EncloseableScope {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String VERSION_VARIABLE_NAME = "version";
    public static final String PROJECT_VERSION_VARIABLE_NAME = "project.version";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    //
    // the associated POM. Through that instance it has access to the higher level of the hierarchy
    //
    private POM pom;
    private InLineXMLEditor editor;

    private VariableReferenceResolver variableReferenceResolver;

    private Scope parent;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * This constructor DOES not establishes the scope hierarchy. The caller must do it.
     *
     * It is important to do it AFTER the constructor completes, to give local properties to be declared correctly.
     *
     * @param pom can never be null.
     */
    public POMScope(POM pom, InLineXMLEditor editor) {

        if (pom == null) {

            throw new IllegalArgumentException("null pom");
        }

        if (editor == null) {

            throw new IllegalArgumentException("null in-line XML editor");
        }

        this.pom = pom;
        this.editor = editor;
        this.variableReferenceResolver = new VariableReferenceResolver();

    }

    // Scope overrides -------------------------------------------------------------------------------------------------

    @Override
    public <T> Variable<T> declare(String name, Class<? extends T> type) {

        throw new UnsupportedOperationException("a POM scope is read-only, it cannot be used to declare variables");
    }

    @Override
    public <T> Variable<T> declare(String name, T value) {

        throw new UnsupportedOperationException("a POM scope is read-only, it cannot be used to declare variables");
    }

    @Override
    public Variable undeclare(String name) {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<Variable> getVariablesDeclaredInScope() {

        List<Variable> result = new ArrayList<>();

        Variable v = getVariable(VERSION_VARIABLE_NAME);

        if (v != null) {

            result.add(v);
        }

        v = getVariable(PROJECT_VERSION_VARIABLE_NAME);

        if (v != null) {

            result.add(v);
        }

        //
        // expose properties declared in the associated XML file as variables
        //

        List<XMLElement> propertyElements = editor.getElements("/project/properties");

        if (!propertyElements.isEmpty()) {

            //noinspection Convert2streamapi
            for(XMLElement p: propertyElements) {

                result.add(new POMVariable(p.getName(), p.getValue()));
            }
        }

        return result;
    }

    @Override
    public Variable getVariable(String name) {

        if (VERSION_VARIABLE_NAME.equals(name) || PROJECT_VERSION_VARIABLE_NAME.equals(name)) {

            try {

                Version v = pom.getVersion();

                //
                // version may be null
                //

                if (v != null) {

                    String s = v.getLiteral();

                    return new POMVariable(name, s);
                }
            }
            catch(VersionFormatException e) {

                throw new IllegalStateException(e);
            }
        }

        List<XMLElement> propertyElements = editor.getElements("/project/properties");

        for(XMLElement p: propertyElements) {

            if (p.getName().equals(name)) {

                return new POMVariable(p.getName(), p.getValue());
            }
        }

        if (parent != null) {

            return parent.getVariable(name);
        }

        return null;
    }

    @Override
    public void enclose(EncloseableScope scope) {

        if (scope == null) {

            throw new IllegalArgumentException("null scope");
        }

        scope.setParent(this);
    }

    @Override
    public String evaluate(String stringWithVariableReferences) {

        try {

            return evaluate(stringWithVariableReferences, false);
        }
        catch(UndeclaredVariableException e) {

            //
            // should not happen
            //
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String evaluate(String stringWithVariableReferences, boolean failOnUndeclaredVariable)
            throws UndeclaredVariableException {

        return variableReferenceResolver.resolve(stringWithVariableReferences, failOnUndeclaredVariable, this);
    }

    @Override
    public Scope getEnclosing() {

        return parent;
    }

    @Override
    public void setParent(Scope parent) {

        this.parent = parent;
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
