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

import io.novaordis.release.version.Version;

import java.io.File;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class ArtifactImpl implements Artifact {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ArtifactType type;
    private File repositoryFile;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * Assumes null final name and extension (in which case will be inferred based on ArtifactType instance)
     */
    public ArtifactImpl(ArtifactType type, String groupId, String artifactId, Version version) {

        this(type, groupId, artifactId, version, null, null);
    }

    /**
     * @param extension if specified, takes priority. If null, the extension will be inferred based on the artifact
     *                  type.
     */
    public ArtifactImpl(ArtifactType type, String groupId, String artifactId,
                        Version version, String finalName, String extension) {

        this.type = type;

        String v = version.getLiteral();

        String s = groupId.replace('.', '/');

        String effectiveExtension = extension != null ? extension : type.getExtension();

        //
        // finalName is ignored when it comes to install artifacts in the repository
        //
        //
        // TODO find a solution to that
        //

        //
        // String artifactBaseName = finalName != null ? finalName : artifactId + "-" + v;
        //

        String artifactBaseName = artifactId + "-" + v;

        //
        //
        //

        s += '/' + artifactId + "/" + v + "/" + artifactBaseName + "." + effectiveExtension;

        this.repositoryFile = new File(s);
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

        if (!(o instanceof ArtifactImpl)) {
            return false;
        }

        ArtifactImpl that = (ArtifactImpl)o;

        return type.equals(that.type) && repositoryFile.equals(that.repositoryFile);
    }

    @Override
    public int hashCode() {

        return type == null ? 0 : type.hashCode() + 7 * (repositoryFile == null ? 0 : repositoryFile.hashCode());
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
