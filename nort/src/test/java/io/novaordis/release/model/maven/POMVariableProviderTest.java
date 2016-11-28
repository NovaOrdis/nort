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

import io.novaordis.release.model.MockInLineXmlEditor;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.xml.editor.XmlElement;
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
public class POMVariableProviderTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(POMVariableProviderTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Test ------------------------------------------------------------------------------------------------------------

    @Test
    public void constructor_NullPom() throws Exception {

        try {
            new POMVariableProvider(null, null);
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
            new POMVariableProvider(new MockPOM(), null);
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
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();

        POMVariableProvider p = new POMVariableProvider(mockPom, mockEditor);

        assertEquals(mockPom, p.getPom());
    }

    // setVariableValue() ----------------------------------------------------------------------------------------------

    @Test
    public void setVariableValue() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();
        POMVariableProvider p = new POMVariableProvider(mockPom, mockEditor);

        try {
            p.setVariableValue("testname", "testvalue");
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
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();
        POMVariableProvider p = new POMVariableProvider(mockPom, mockEditor);

        mockPom.setVersion(new Version("5678"));

        String s = p.getVariableValue("version");
        assertEquals("5678", s);
    }

    @Test
    public void getVariableValue_ProjectVersion() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();
        POMVariableProvider p = new POMVariableProvider(mockPom, mockEditor);

        mockPom.setVersion(new Version("6789"));

        String s = p.getVariableValue("project.version");
        assertEquals("6789", s);
    }

    @Test
    public void getVariableValue_DeclaredProperty() throws Exception {

        MockPOM mockPom = new MockPOM();
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();
        mockEditor.setElements("/project/properties", new XmlElement("declared.property", "something-123"));

        POMVariableProvider p = new POMVariableProvider(mockPom, mockEditor);

        String s = p.getVariableValue("declared.property");
        assertEquals("something-123", s);
    }

    @Test
    public void getVariableValue_DeclaredPropertyInParent() throws Exception {

        MockPOM mockParentPom = new MockPOM();
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();
        mockEditor.setElements("/project/properties", new XmlElement("declared.property", "something-345"));
        POMVariableProvider parentProvider = new POMVariableProvider(mockParentPom, mockEditor);
        mockParentPom.setVariableProvider(parentProvider);

        MockPOM pom = new MockPOM();
        MockInLineXmlEditor mockEditor2 = new MockInLineXmlEditor();
        POMVariableProvider provider = new POMVariableProvider(pom, mockEditor2);
        pom.setVariableProvider(provider);

        pom.setParent(mockParentPom);

        String s = provider.getVariableValue("declared.property");
        assertEquals("something-345", s);
    }

    @Test
    public void getVariableValue_NoSuchVariable() throws Exception {

        MockPOM mockParentPom = new MockPOM();
        MockInLineXmlEditor mockEditor = new MockInLineXmlEditor();
        mockEditor.setElements("/project/properties", new XmlElement("declared.property", "something-345"));
        POMVariableProvider parentProvider = new POMVariableProvider(mockParentPom, mockEditor);
        mockParentPom.setVariableProvider(parentProvider);

        MockPOM pom = new MockPOM();
        MockInLineXmlEditor mockEditor2 = new MockInLineXmlEditor();
        POMVariableProvider provider = new POMVariableProvider(pom, mockEditor2);
        pom.setVariableProvider(provider);

        pom.setParent(mockParentPom);

        String s = provider.getVariableValue("no.such.property");
        assertNull(s);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
