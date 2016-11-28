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

package io.novaordis.release.model;

import io.novaordis.utilities.xml.editor.InLineXmlEditor;
import io.novaordis.utilities.xml.editor.XmlElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/28/16
 */
public class MockInLineXmlEditor implements InLineXmlEditor {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Map<String, List<XmlElement>> paths;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockInLineXmlEditor() {

        this.paths = new HashMap<>();
    }

    // InLineXmlEditor implementation ----------------------------------------------------------------------------------

    @Override
    public File getFile() {
        throw new RuntimeException("getFile() NOT YET IMPLEMENTED");
    }

    @Override
    public int getLineCount() {
        throw new RuntimeException("getLineCount() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean isDirty() {
        throw new RuntimeException("isDirty() NOT YET IMPLEMENTED");
    }

    @Override
    public String getContent() {
        throw new RuntimeException("getContent() NOT YET IMPLEMENTED");
    }

    @Override
    public String get(String path) {
        throw new RuntimeException("get() NOT YET IMPLEMENTED");
    }

    @Override
    public List<String> getList(String path) {
        throw new RuntimeException("getList() NOT YET IMPLEMENTED");
    }

    @Override
    public List<XmlElement> getElements(String path) {

        List<XmlElement> elements = paths.get(path);

        if (elements == null) {
            return Collections.emptyList();
        }

        return elements;
    }

    @Override
    public boolean set(String path, String newValue) {
        throw new RuntimeException("set() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean save() throws IOException {
        throw new RuntimeException("save() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean undo() throws IOException {
        throw new RuntimeException("undo() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setElements(String path, XmlElement... elements) {

        if (elements == null) {
            return;
        }

        List<XmlElement> storage = paths.get(path);

        if (storage == null) {

            storage = new ArrayList<>();
            paths.put(path, storage);
        }

        Collections.addAll(storage, elements);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
