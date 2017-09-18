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
import io.novaordis.utilities.xml.editor.XMLElement;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
    public void constructor() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();

        POMScope p = new POMScope(mockPom, mockEditor);

        assertEquals(mockPom, p.getPom());
    }

    // setVariableValue() ----------------------------------------------------------------------------------------------

    @Test
    public void setVariableValue() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        POMScope p = new POMScope(mockPom, mockEditor);

        try {

            p.declare("testname", "testvalue");
            fail("should throw exception");
        }
        catch(UnsupportedOperationException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("a POM variable provider is read-only, it cannot be used to set variable values", msg);
        }
    }

    // getVariableValue() ----------------------------------------------------------------------------------------------

    @Test
    public void getVariableValue_Version() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        POMScope p = new POMScope(mockPom, mockEditor);

        mockPom.setVersion(new Version("5678"));

        String s = (String)p.getVariable("version").get();
        assertEquals("5678", s);
    }

    @Test
    public void getVariableValue_ProjectVersion() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        POMScope p = new POMScope(mockPom, mockEditor);

        mockPom.setVersion(new Version("6789"));

        String s = (String)p.getVariable("project.version").get();
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
        mockParentPom.setVariableProvider(parentProvider);

        MockPOM pom = new MockPOM();
        MockInLineXMLEditor mockEditor2 = new MockInLineXMLEditor();
        POMScope scope = new POMScope(pom, mockEditor2);
        pom.setVariableProvider(scope);

        pom.setParent(mockParentPom);

        String s = (String)scope.getVariable("declared.property").get();
        assertEquals("something-345", s);
    }

    @Test
    public void getVariableValue_NoSuchVariable() throws Exception {

        MockPOM mockParentPom = new MockPOM();
        MockInLineXMLEditor mockEditor = new MockInLineXMLEditor();
        mockEditor.setElements("/project/properties", new XMLElement("declared.property", "something-345"));
        POMScope parentProvider = new POMScope(mockParentPom, mockEditor);
        mockParentPom.setVariableProvider(parentProvider);

        MockPOM pom = new MockPOM();
        MockInLineXMLEditor mockEditor2 = new MockInLineXMLEditor();
        POMScope scope = new POMScope(pom, mockEditor2);
        pom.setVariableProvider(scope);

        pom.setParent(mockParentPom);

        String s = (String)scope.getVariable("no.such.property").get();
        assertNull(s);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
