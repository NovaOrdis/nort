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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An in-memory representation of a Maven project, which may include one or multiple POM files.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class MavenProject implements Project {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MavenProject.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // POM maintains and interfaces with an individual POM file only, the recursive multi-module structure is
    // maintained as "modules"
    private POM root;

    private List<MavenModule> modules;

    private ProjectVersioningModel versioningModel;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @exception Exception all exceptions thrown by this constructor should have a descriptive, human-understandable
     * error message.
     */
    public MavenProject() throws Exception {

        this(null);
    }

    /**
     * @param rootPomFile the top level pom.xml file. The file may contain sub-module references.
     *
     * @exception Exception all exceptions thrown by this constructor should have a descriptive, human-understandable
     * error message.
     *
     * @exception io.novaordis.utilities.UserErrorException on invalid Maven metadata.
     */
    public MavenProject(File rootPomFile) throws Exception {

        this.modules = new ArrayList<>();
        this.versioningModel = ProjectVersioningModel.SINGLE_MODULE;

        if (rootPomFile == null) {
            return;
        }

        root = new POM(rootPomFile);

        //
        // link to modules, if any
        //

        List<String> moduleNames = root.getModuleNames();

        if (moduleNames.isEmpty()) {
            return;
        }

        //
        // we have modules, for the time being we assume the lockstep versioning model; if, in the future, we need
        // support for independent versions, we'll add it then.
        //

        this.versioningModel = ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP;

        for(String moduleName: moduleNames) {

            File moduleDir = new File(rootPomFile.getParentFile(), moduleName);

            if (!moduleDir.isDirectory()) {
                throw new UserErrorException("no module directory " + moduleDir.getAbsolutePath());
            }

            MavenModule m = new MavenModule(this, new File(moduleDir, "pom.xml"));
            addModule(m);
        }

        log.debug(this + " created");
    }

    // Project implementation ------------------------------------------------------------------------------------------

    @Override
    public String getName() {

        if (root == null) {

            return null;
        }

        return root.getArtifactId();
    }

    /**
     * @return null on an uninitialized instance
     */
    @Override
    public Version getVersion() throws VersionFormatException {

        return root != null ? root.getVersion() : null;
    }

    @Override
    public boolean setVersion(Version version) {

        return root.setVersion(version);
    }

    /**
     * @return null on an uninitialized instance.
     */
    @Override
    public File getBaseDirectory() {

        if (root == null) {
            return null;
        }

        return root.getFile().getParentFile();
    }

    @Override
    public File getFile() {

        if (root == null) {
            return null;
        }

        return root.getFile();
    }

    /**
     * Also see:
     *
     * @see POM#save()
     * @see InLineXmlEditor#save()
     */
    @Override
    public boolean save() throws IOException {

        if (root == null) {
            throw new IOException("attempt to save an uninitialized project instance");
        }

        return root.save();
    }

    /**
     * Also see:
     *
     * @see POM#undo()
     * @see InLineXmlEditor#undo()
     */
    @Override
    public boolean undo() throws IOException {

        return root != null && root.undo();
    }

    @Override
    public Set<ArtifactType> getArtifactTypes() {

        Set<ArtifactType> result = new HashSet<>();

        //noinspection Convert2streamapi
        for(MavenModule m: modules) {

            result.add(m.getArtifactType());
        }

        ArtifactType t = root == null ? null : root.getArtifactType();

        if (t != null) {

            result.add(t);
        }

        return result;
    }

    @Override
    public List<Artifact> getArtifacts() {

        return getArtifacts(null);
    }

    /**
     * @param artifactType null is acceptable, will return all artifacts.
     */
    @Override
    public List<Artifact> getArtifacts(ArtifactType artifactType) {

        List<Artifact> result = new ArrayList<>();

        //noinspection Convert2streamapi
        for(MavenModule m: modules) {

            Artifact a = m.getArtifact();

            if (artifactType == null || artifactType.equals(a.getType())) {

                result.add(a);
            }
        }

        Artifact t = root.getArtifact();

        if (t != null && (artifactType == null || artifactType.equals(t.getType()))) {

            result.add(t);
        }

        return result;
    }

    @Override
    public String toString() {

        String s = getName();
        return s == null ? "null" : s;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public POM getPOM() {

        return root;
    }

    /**
     * @return may return null
     */
    public MavenModule getModule(String moduleName) {

        for(MavenModule m: modules) {
            if (m.getName().equals(moduleName)) {
                return m;
            }
        }

        return null;
    }

    public ProjectVersioningModel getVersioningModel() {

        return versioningModel;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void addModule(MavenModule m) {

        modules.add(m);
    }

    // Protected -------------------------------------------------------------------------------------------------------

    protected void setPOM(POM pom) {

        this.root = pom;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
