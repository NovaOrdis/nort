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

import io.novaordis.release.model.Artifact;
import io.novaordis.release.model.ArtifactImpl;
import io.novaordis.release.model.ArtifactType;
import io.novaordis.release.model.Project;
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.xml.editor.InLineXmlEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper around read-only metadata from a pom file and an editor capable of writing read-write information, such
 * as version.
 *
 * It <tt>only</tt> contains metadata pertaining to the project root or module the corresponding pom file is associated
 * with. The recursive module information is maintained outside this class, by MavenProject.
 *
 * @see MavenProject
 * @see MavenModule
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class POM {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(POM.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private InLineXmlEditor pomEditor;

    // may be null
    private POM parent;

    //
    // read-only information - this information does not change during release
    //
    private String groupId;
    private String artifactId;
    @SuppressWarnings("FieldCanBeLocal")
    private String packaging;

    private ArtifactType artifactType;

    private List<String> moduleNames;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Constructor without parent.
     *
     * @param pomFile the associated pom.xml file.
     *
     * @see POM(POM, File)
     *
     * @exception Exception all exceptions thrown by this constructor should have a descriptive, human-understandable
     * error message.
     */
    public POM(File pomFile) throws Exception {

        this(null, pomFile);
    }

    /**
     * @param pomFile the associated pom.xml file.
     *
     * @param parent may be null. Provided in case of a module POM, that has a parent.
     *
     * @exception Exception all exceptions thrown by this constructor should have a descriptive, human-understandable
     * error message.
     */
    public POM(POM parent, File pomFile) throws Exception {

        this.parent = parent;

        pomEditor = new InLineXmlEditor(pomFile);

        //
        // cache the read-only information
        //

        this.groupId = pomEditor.get("/project/groupId");

        if (groupId == null) {

            //
            // get the groupId from the parent, if exists
            //

            if (parent != null) {
                groupId = parent.getGroupId();
            }
        }

        if (groupId == null) {
            throw new UserErrorException("missing groupId");
        }

        this.artifactId = pomEditor.get("/project/artifactId");
        this.packaging = pomEditor.get("/project/packaging");
        this.artifactType = ArtifactType.fromString(packaging);

        if ("pom".equals(packaging)) {

            //
            // look for modules
            //

            moduleNames = pomEditor.getList("/project/modules/module");

            if (moduleNames.isEmpty()) {
                throw new RuntimeException("NOT YET IMPLEMENTED: we don't know what to do with 'pom' packaging and no modules");
            }
        }
        else {

            moduleNames = Collections.emptyList();
        }
    }

    /**
     * For testing only.
     */
    protected POM() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the associated pom file.
     */
    public File getFile() {

        if (pomEditor == null) {
            return null;
        }

        return pomEditor.getFile();
    }

    public String getGroupId() {

        return groupId;
    }

    public Version getVersion() throws VersionFormatException {

        return new Version(pomEditor.get("/project/version"));
    }

    /**
     * Modify the version information, as cached in memory. In order to write the modification on disk, call save().
     *
     * @param version the instance is syntactically correct, as it was already parsed.
     *
     * @see Project#save()
     * @see InLineXmlEditor#save()
     *
     * @return true if the version information was changed in memory as result of the call, false otherwise, which
     * happens when the cached version is the same as the version passed as argument.
     */
    public boolean setVersion(Version version) {

        boolean changed = pomEditor.set("/project/version", version.getLiteral());

        if (changed) {
            log.debug("modified in-memory version to " + version);
        }

        return changed;
    }

    public ArtifactType getArtifactType() {

        return artifactType;
    }

    /**
     * A POM can have just one or zero artifacts. Projects can have more than one.
     *
     * @see Project#getArtifacts()
     *
     * @return an artifact or null. A root pom for a multi-module projects does not have an artifact.
     */
    public Artifact getArtifact() {

        //
        // "pom" packaging is module-less
        //
        if (artifactType == null) {
            return null;
        }

        //
        // we dynamically create the instance every time is requested, as the underlying version is bound to change
        //

        Version v;

        try {

            v = getVersion();
        }
        catch (VersionFormatException e) {

            //
            // we don't expect a format exception, but if it happens, we need to push it up
            //

            throw new IllegalArgumentException("invalid version in the underlying POM file", e);
        }

        return new ArtifactImpl(artifactType, groupId, artifactId, v);
    }

    /**
     * @return the list of module names, in order, as declared in pom.xml. May return an empty list, but never null.
     */
    public List<String> getModuleNames() {

        return moduleNames;
    }

    /**
     * @return may return null
     */
    public POM getParent() {

        return parent;
    }

    /**
     * @see InLineXmlEditor#save()
     */
    public boolean save() throws IOException {

        return pomEditor.save();
    }

    /**
     * @see InLineXmlEditor#undo()
     */
    public boolean undo() throws IOException {

        return pomEditor.undo();
    }

    @Override
    public String toString() {

        File file = getFile();

        if (file == null) {
            return "null";
        }

        return file.getAbsolutePath();
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
