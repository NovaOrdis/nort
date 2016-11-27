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

    private String finalName;

    private String extension;

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

        this.moduleNames = Collections.emptyList();

        this.parent = parent;

        pomEditor = new InLineXmlEditor(pomFile);

        //
        // cache the read-only information
        //

        this.groupId = resolveGroupId(pomEditor);
        this.artifactId = pomEditor.get("/project/artifactId");
        this.packaging = pomEditor.get("/project/packaging");
        this.artifactType = ArtifactType.fromString(packaging);
        this.extension = artifactType == null ? null : artifactType.getExtension();

        if ("pom".equals(packaging)) {
            handlePomPackaging(parent, pomEditor);
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

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {

        return groupId;
    }

    /**
     * If the version is not specified in the file, and there is a parent, the method will attempt to get the version
     * from the parent. May still return null.
     */
    public Version getVersion() throws VersionFormatException {

        Version localVersion = getLocalVersion();

        if (localVersion == null) {

            //
            //  no version information, it's probably inherited from the parent, try the parent
            //
            //

            if (parent == null) {

                return null;
            }

            return parent.getVersion();
        }

        return localVersion;
    }

    /**
     * @return the version written in the file, or null if not available, without attempting to delegate to parent.
     */
    public Version getLocalVersion() throws VersionFormatException {

        if (pomEditor == null) {

            return null;
        }

        String s = pomEditor.get("/project/version");

        if (s == null) {

            return null;
        }

        return new Version(s);
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

        if (artifactType == null) {

            return null;
        }

        //
        // we dynamically create the instance every time is requested, as the underlying version is bound to change
        //

        try {

            return new ArtifactImpl(artifactType, groupId, artifactId, getVersion(), getFinalName(), getExtension());
        }
        catch(VersionFormatException e) {

            // we don't expect a format exception, but if it happens, we need to push it up

            throw new IllegalArgumentException("invalid version in the underlying POM file", e);
        }
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
     * @return the string corresponding to the <finalName> element in declared in <build> (or the corresponding
     * <finalName> declared in the assembly configuration of a release module. May return null, if the POM does
     * not explicitly declare a <finalName>.
     */
    public String getFinalName() {

        return finalName;
    }

    /**
     * The extension for the artifacts corresponding to this POM. null if the POM does not produce artifacts. In
     * most cases, it's the "packaging" value, but there are some situations where isn't: for example when the POM
     * corresponds to a release module (packaging is "pom") which uses an assembly to build the artifact. In that case
     * the extension is the assembly's <format>.
     */
    public String getExtension() {

        return extension;
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

    /**
     * Resolves the groupId to a non-null string - from the current pom or the parent. If not able to find a groupId,
     * throws a UserErrorException.
     */
    private String resolveGroupId(InLineXmlEditor editor) throws Exception {

        String gid = editor.get("/project/groupId");

        if (gid != null) {

            return gid;

        }

        //
        // attempt get the groupId from the parent, if exists
        //

        gid = editor.get("/project/parent/groupId");

        if (gid != null) {
            return gid;
        }

        throw new UserErrorException("missing groupId");
    }

    /**
     * "pom" packaging requires special handling. A POM file that declares "pom" packaging is either a project root
     * POM, and in this case declares a list of module, or a "release" module
     * (https://kb.novaordis.com/index.php/Building_a_Maven_Complex_Release_Artifact#Dedicated_Release_Module), which
     * builds a binary distribution.
     */
    private void handlePomPackaging(POM parentPom, InLineXmlEditor editor) throws Exception {

        List<String> mns = editor.getList("/project/modules/module");

        if (mns != null && !mns.isEmpty()) {

            //
            // we found modules, save them as part of the read-only state of this instance
            //
            this.moduleNames = mns;
        }
        else {

            //
            // we did not find module names, so the only option at this point is this is a "release" module
            //

            processReleaseModulePom(parentPom, editor);
        }
    }

    /**
     * Logic to execute when we established this is a release module POM - initializes specific state. For more
     * details about release modules see
     * https://kb.novaordis.com/index.php/Building_a_Maven_Complex_Release_Artifact#Dedicated_Release_Module.
     * A release module builds a binary distribution.
     */
    private void processReleaseModulePom(POM parentPom, InLineXmlEditor editor) throws Exception {

        if (parentPom == null) {

            throw new UserErrorException(
                    "invalid 'pom' packaging POM file, no modules and no parent " + editor.getFile().getAbsolutePath());
        }

        //
        // we're a module, make sure we're a release module
        //

        String thisModuleName = getFile().getParentFile().getName();

        this.artifactType = ArtifactType.BINARY_DISTRIBUTION;

        //
        // insure we have an assembly plugin
        //

        List<String> plugins = editor.getList("/project/build/plugins/plugin/artifactId");

        if (!plugins.contains("maven-assembly-plugin")) {
            throw new UserErrorException(
                    "the release module '" + thisModuleName + "' does not contain an assembly plugin");
        }

        //
        // resolve finalName
        //

        this.finalName = editor.get("/project/build/plugins/plugin/configuration/finalName");

        //
        // resolve extension - we read the assembly and extract the format from there
        //

        String assemblyFileRelativePath = editor.get(
                "/project/build/plugins/plugin/configuration/descriptors/descriptor");
        File assemblyFile = new File(getFile().getParentFile(), assemblyFileRelativePath);

        if (!assemblyFile.isFile() || !assemblyFile.canRead()) {

            throw new UserErrorException(
                    "assembly descriptor " + assemblyFile.getAbsolutePath() + " not available or cannot be read");
        }

        InLineXmlEditor assemblyEditor = new InLineXmlEditor(assemblyFile);

        List<String> formats = assemblyEditor.getList("/assembly/formats/format");

        if (formats.size() > 1) {
            throw new RuntimeException("NOT YET IMPLEMENTED: don't know how to handle more than one formats");
        }

        this.extension = formats.get(0);

        //
        // check conventions and issue warnings if we don't comply
        //

        if (!"release".equals(thisModuleName)) {
            log.warn("a release module should be conventionally named 'release', but in this case it is named '" + thisModuleName + "'");
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
