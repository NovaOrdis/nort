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


import io.novaordis.release.model.ArtifactType;
import io.novaordis.release.version.Version;

import java.io.File;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class MavenArtifactImpl implements MavenArtifact {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ArtifactType type;
    private File repositoryFile;

    private String localArtifactBaseName;

    private String extension;

    // the pom this artifact is associated with. Never null.
    private POM pom;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param pom the POM this artifact is associated with. Must never be null.
     * @param ext if specified, takes priority. If null, the extension will be inferred based on the artifact type.
     */
    public MavenArtifactImpl(POM pom, ArtifactType type, String groupId, String artifactId,
                             Version version, String finalName, String ext) {

        if (pom == null) {
            throw new IllegalArgumentException("null pom");
        }

        this.pom = pom;
        this.type = type;

        String v = version.getLiteral();

        String groupSection = groupId.replace('.', '/');

        this.extension = ext != null ? ext : type.getExtension();

        //
        // finalName is ignored when it comes to install artifacts in the repository; it is reflected though
        // in the name of the local file.
        //

        String repositoryArtifactBaseName = artifactId + "-" + v;

        this.repositoryFile =
                new File(groupSection + '/' + artifactId + "/" + v + "/" + repositoryArtifactBaseName + "." + extension);

        this.localArtifactBaseName = finalName != null ? finalName : artifactId + "-" + v;
    }

    // Artifact implementation -----------------------------------------------------------------------------------------

    @Override
    public ArtifactType getType() {

        return type;
    }

    @Override
    public File getRepositoryFile() {

        return repositoryFile;
    }

    @Override
    public File getLocalFile() {

        String pathRelativeToProjectHome = "";

        MavenModule m = pom.getModule();

        if (m != null) {
            pathRelativeToProjectHome = m.getName() + "/";
        }

        return new File(pathRelativeToProjectHome + "target/" + localArtifactBaseName + "." + extension);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (type == null) {
            return false;
        }

        if (repositoryFile == null) {
            return false;
        }

        if (!(o instanceof MavenArtifactImpl)) {
            return false;
        }

        MavenArtifactImpl that = (MavenArtifactImpl)o;

        return type.equals(that.type) && repositoryFile.equals(that.repositoryFile);
    }

    @Override
    public int hashCode() {

        return type == null ? 0 : type.hashCode() + 7 * (repositoryFile == null ? 0 : repositoryFile.hashCode());
    }

    // MavenArtifact implementation ------------------------------------------------------------------------------------

    /**
     * @return the POM instance this artifact is associated with. Can never ve null.
     */
    @Override
    public POM getPOM() {

        return pom;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "" + type + ":" + getRepositoryFile();
    }


    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
