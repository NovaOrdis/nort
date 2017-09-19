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

import io.novaordis.release.model.MockInLineXMLEditor;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.expressions.ScopeImpl;
import io.novaordis.utilities.expressions.UndeclaredVariableException;
import io.novaordis.utilities.expressions.Variable;
import io.novaordis.utilities.xml.editor.BasicInLineXMLEditor;
import io.novaordis.utilities.xml.editor.InLineXMLEditor;
import io.novaordis.utilities.xml.editor.XMLElement;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/27/16
 */
public class POMScopeTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(POMScopeTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Test ------------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullPom() throws Exception {

        try {
            new POMScope(null, null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null pom", msg);
        }
    }

    @Test
    public void constructor_NullEditor() throws Exception {

        try {
            new POMScope(new MockPOM(), null);
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("null in-line XML editor", msg);
        }
    }

    @Test
    public void constructor_NoVersionInformation() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();

        POMScope p = new POMScope(mockPom, mockEditor);

        assertEquals(mockPom, p.getPom());

        assertNull(p.getVariable(POMScope.VERSION_VARIABLE_NAME));
        assertNull(p.getVariable(POMScope.PROJECT_VERSION_VARIABLE_NAME));
    }

    @Test
    public void constructor() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();

        mockPom.setVersion(new Version("1.0"));

        POMScope p = new POMScope(mockPom, mockEditor);

        assertEquals(mockPom, p.getPom());

        Variable v = p.getVariable(POMScope.VERSION_VARIABLE_NAME);
        assertEquals("1.0", v.get());

        Variable v2 = p.getVariable(POMScope.PROJECT_VERSION_VARIABLE_NAME);
        assertEquals("1.0", v2.get());
    }

    // getVariableValue() ----------------------------------------------------------------------------------------------

    @Test
    public void getVariableValue_Version() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        POMScope p = new POMScope(mockPom, mockEditor);

        mockPom.setVersion(new Version("5678"));

        String s = (String)p.getVariable(POMScope.VERSION_VARIABLE_NAME).get();
        assertEquals("5678", s);
    }

    @Test
    public void getVariableValue_ProjectVersion() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        POMScope p = new POMScope(mockPom, mockEditor);

        mockPom.setVersion(new Version("6789"));

        String s = (String)p.getVariable(POMScope.PROJECT_VERSION_VARIABLE_NAME).get();
        assertEquals("6789", s);
    }

    @Test
    public void getVariableValue_DeclaredProperty() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        mockEditor.setElements("/project/properties", new XMLElement("declared.property", "something-123"));

        POMScope p = new POMScope(mockPom, mockEditor);

        String s = (String)p.getVariable("declared.property").get();
        assertEquals("something-123", s);
    }

    @Test
    public void getVariableValue_DeclaredPropertyInParent() throws Exception {

        MockPOM mockParentPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        mockEditor.setElements("/project/properties", new XMLElement("declared.property", "something-345"));
        POMScope parentProvider = new POMScope(mockParentPom, mockEditor);
        mockParentPom.setScope(parentProvider);

        MockPOM mp = new MockPOM();
        MockInLineXMLEditor mockEditor2 = new MockInLineXMLEditor();

        POMScope scope = new POMScope(mp, mockEditor2);

        mp.setScope(scope);

        mp.setParent(mockParentPom);

        String s = (String)scope.getVariable("declared.property").get();

        assertEquals("something-345", s);
    }

    @Test
    public void getVariableValue_NoSuchVariable() throws Exception {

        MockPOM mockParentPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        mockEditor.setElements("/project/properties", new XMLElement("declared.property", "something-345"));

        POMScope parentScope = new POMScope(mockParentPom, mockEditor);
        mockParentPom.setScope(parentScope);

        MockPOM mp = new MockPOM();
        MockInLineXMLEditor mockEditor2 = new MockInLineXMLEditor();

        POMScope scope = new POMScope(mp, mockEditor2);

        mp.setScope(scope);

        mp.setParent(mockParentPom);

        Variable v = scope.getVariable("no.such.property");
        assertNull(v);
    }

    // EncloseableScope interface testing ------------------------------------------------------------------------------

    @Test
    public void declare() throws Exception {

        MockPOM mp = new MockPOM();
        MockInLineXMLEditor me = new MockInLineXMLEditor();

        POMScope s = new POMScope(mp, me);

        try {

            s.declare("test", String.class);
            fail("should have thrown exception");
        }
        catch(UnsupportedOperationException e) {

            //
            // noop
            //

            String msg = e.getMessage();
            assertEquals("a POM scope is read-only, it cannot be used to declare variables", msg);
        }
    }

    @Test
    public void declare2() throws Exception {

        MockPOM mp = new MockPOM();
        MockInLineXMLEditor me = new MockInLineXMLEditor();

        POMScope s = new POMScope(mp, me);

        try {

            s.declare("test", "something");
            fail("should have thrown exception");
        }
        catch(UnsupportedOperationException e) {

            //
            // noop
            //

            String msg = e.getMessage();
            assertEquals("a POM scope is read-only, it cannot be used to declare variables", msg);
        }
    }

    @Test
    public void undeclare() throws Exception {

        MockPOM mp = new MockPOM();
        MockInLineXMLEditor me = new MockInLineXMLEditor();

        POMScope s = new POMScope(mp, me);

        try {

            s.undeclare("test");
            fail("should have thrown exception");
        }
        catch(UnsupportedOperationException e) {

            //
            // noop
            //
        }
    }

    @Test
    public void getVariablesDeclaredInScope() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        List<Variable> vars = s.getVariablesDeclaredInScope();

        assertEquals(4, vars.size());

        Variable v = vars.get(0);
        assertEquals(POMScope.VERSION_VARIABLE_NAME, v.name());
        assertEquals("7.7.7", v.get());

        Variable v2 = vars.get(1);
        assertEquals(POMScope.PROJECT_VERSION_VARIABLE_NAME, v2.name());
        assertEquals("7.7.7", v2.get());

        Variable v3 = vars.get(2);
        assertEquals("something", v3.name());
        assertEquals("a", v3.get());

        Variable v4 = vars.get(3);
        assertEquals("something.else", v4.name());
        assertEquals("b", v4.get());
    }

    @Test
    public void getVariable() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        Variable nsv = s.getVariable("i.am.pretty.sure.such.a.variable.does.not.exist");
        assertNull(nsv);

        Variable v = s.getVariable(POMScope.VERSION_VARIABLE_NAME);
        assertEquals(POMScope.VERSION_VARIABLE_NAME, v.name());
        assertEquals("7.7.7", v.get());

        Variable v2 = s.getVariable(POMScope.PROJECT_VERSION_VARIABLE_NAME);
        assertEquals(POMScope.PROJECT_VERSION_VARIABLE_NAME, v2.name());
        assertEquals("7.7.7", v2.get());

        Variable v3 = s.getVariable("something");
        assertEquals("something", v3.name());
        assertEquals("a", v3.get());

        Variable v4 = s.getVariable("something.else");
        assertEquals("something.else", v4.name());
        assertEquals("b", v4.get());
    }

    @Test
    public void enclose_Null() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        try {

            s.enclose(null);

            fail("should have thrown exception");
        }
        catch(IllegalArgumentException e2) {

            assertTrue(e2.getMessage().contains("null scope"));
        }

    }

    @Test
    public void enclose() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        ScopeImpl enclosed = new ScopeImpl();

        assertNull(enclosed.getVariable(POMScope.VERSION_VARIABLE_NAME));
        assertNull(enclosed.getVariable(POMScope.PROJECT_VERSION_VARIABLE_NAME));
        assertNull(enclosed.getVariable("something"));
        assertNull(enclosed.getVariable("something.else"));

        s.enclose(enclosed);

        Variable v = enclosed.getVariable(POMScope.VERSION_VARIABLE_NAME);
        assertEquals(POMScope.VERSION_VARIABLE_NAME, v.name());
        assertEquals("7.7.7", v.get());

        Variable v2 = enclosed.getVariable(POMScope.PROJECT_VERSION_VARIABLE_NAME);
        assertEquals(POMScope.PROJECT_VERSION_VARIABLE_NAME, v2.name());
        assertEquals("7.7.7", v2.get());

        Variable v3 = enclosed.getVariable("something");
        assertEquals("something", v3.name());
        assertEquals("a", v3.get());

        Variable v4 = enclosed.getVariable("something.else");
        assertEquals("something.else", v4.name());
        assertEquals("b", v4.get());
    }

    @Test
    public void evaluate_ContainsUndeclaredVariables() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        String result = s.evaluate("no ${such} variable");
        assertEquals("no ${such} variable", result);

        try {

            s.evaluate("no ${such} variable", true);
            fail("should have thrown exception");
        }
        catch(UndeclaredVariableException e2) {

            assertEquals("such", e2.getUndeclaredVariableName());
        }
    }

    @Test
    public void evaluate() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        String result = s.evaluate("${something} horse");
        assertEquals("a horse", result);
    }

    @Test
    public void setParent_getEnclosing() throws Exception {

        File f = new File(System.getProperty("basedir"),
                "src/test/resources/data/maven/poms-with-variables/pom-with-variable-3.xml");

        assertTrue(f.isFile());

        POM p = new POM(f);
        InLineXMLEditor e = new BasicInLineXMLEditor(f);

        POMScope s = new POMScope(p, e);

        assertNull(s.getEnclosing());

        assertNull(s.getVariable("color"));

        ScopeImpl parent = new ScopeImpl();
        parent.declare("color", "blue");

        s.setParent(parent);

        Variable v = s.getVariable("color");

        assertEquals("blue", v.get());

        assertEquals(parent, s.getEnclosing());
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
