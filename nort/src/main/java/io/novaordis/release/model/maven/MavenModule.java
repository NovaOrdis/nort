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
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.xml.editor.InLineXmlEditor;

import java.io.File;
import java.io.IOException;

/**
 * The metadata associated with a maven module. Each module has a POM, and the POM maintains a reference to the module
 * it belongs to.
 *
 * The name of the module is the name of the directory that holds it - this is how it is referred from the parent pom.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class MavenModule {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private MavenProject project;
    private POM pom;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MavenModule(MavenProject p, File pomFile) throws Exception {

        this(p, new POM(p.getPOM(), pomFile));
    }

    /**
     * Used for testing.
     */
    MavenModule(MavenProject p, POM pom) throws Exception {

        this.project = p;
        this.pom = pom;

        //
        // establish the bi-directional relationship between POM and module
        //

        pom.setModule(this);

        //
        // consistency checks
        //

        if (!project.getPOM().equals(pom.getParent())) {
            throw new IllegalArgumentException(
                    "the POM associated with the the project " + p + " differs from the this POM's parent");
        }

        //
        // verify versioning model consistency
        //

        ProjectVersioningModel versioningModel = p.getVersioningModel();
        Version localVersion = pom.getLocalVersion();

        if (versioningModel.equals(ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP) && localVersion != null) {

            throw new UserErrorException(
                    "we only support lockstep versioning mode, yet the project " + p.getName() +
                            " seems to contain independent module versions (" + pom.getFile().getAbsolutePath() + ")");
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the name of the module, which is the name of the directory that holds it - this is how it is referred
     * from the parent pom.
     *
     * TODO: currently we're using this information in relative paths. However, what if the module is several
     * sub-directories down?
     */
    public String getName() {

        if (pom == null) {
            return null;
        }

        File pomFile = pom.getFile();

        if (pomFile == null) {
            return null;
        }

        return pomFile.getParentFile().getName();
    }

    /**
     * @return the parent project.
     */
    public MavenProject getProject() {

        return project;
    }

    public ArtifactType getArtifactType() {

        return pom.getArtifactType();
    }

    /**
     * @return always a non-null instance
     */
    public Artifact getArtifact() {

        Artifact a = pom.getArtifact();

        if (a == null) {
            throw new IllegalArgumentException("this module has no artifact");
        }

        return a;
    }

    public Version getVersion() throws VersionFormatException {
        return pom.getVersion();
    }

    /**
     * If the project is in lockstep versioning mode, it is not allowed to set version independently on modules,
     * so the invocation will throw an UnsupportedOperationException.
     */
    public boolean setVersion(Version v) throws UnsupportedOperationException {

        if (project.getVersioningModel().equals(ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP)) {
            throw new UnsupportedOperationException("cannot independently set version on modules in " +
                    ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP + " versioning mode");
        }

        return pom.setVersion(v);
    }

    public POM getPOM() {
        return pom;
    }

    /**
     * Also see:
     *
     * @see MavenProject#save()
     * @see POM#save()
     */
    public boolean save() throws IOException {

        return pom != null && pom.save();
    }

    /**
     * Also see:
     *
     * @see MavenProject#undo()
     * @see POM#undo()
     * @see InLineXmlEditor#undo()
     */
    public boolean undo() throws IOException {

        return pom != null && pom.undo();
    }

    @Override
    public String toString() {

        String name = getName();
        return name == null ? "null" : name;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
