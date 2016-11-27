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

import java.io.File;

/**
 * The metadata associated with a maven module. Each module has a POM.
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
    MavenModule(MavenProject p, POM pom) {

        this.project = p;
        this.pom = pom;

        //
        // consistency check
        //

        if (!project.getPOM().equals(pom.getParent())) {
            throw new IllegalArgumentException(
                    "the POM associated with the the project " + p + " differs from the this POM's parent");
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

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

    /**
     * @return the name of the module, which is the name of the directory that holds it - this is how it is referred
     * from the parent pom.
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

    public Version getVersion() throws VersionFormatException {
        return pom.getVersion();
    }

    public boolean setVersion(Version v) {

        return pom.setVersion(v);
    }

    /**
     * @return the parent project.
     */
    public MavenProject getProject() {

        return project;
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
