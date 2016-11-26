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
import java.io.IOException;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/25/16
 */
public class MockPOM extends POM {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ArtifactType artifactType;
    private Artifact artifact;
    private File file;
    private String groupId;
    private POM parent;
    private Version version;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockPOM() throws Exception {

        super();

        version = new Version("1");
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    public File getFile() {

        return file;
    }

    @Override
    public String getGroupId() {

        return groupId;
    }

    @Override
    public Version getVersion() throws VersionFormatException {

        return version;
    }

    @Override
    public boolean setVersion(Version version) {

        if (this.version == null) {

            if (version == null) {
                return false;
            }

            this.version = version;
            return true;
        }
        else {

            if (this.version.equals(version)) {
                return false;
            }

            this.version = version;
            return true;
        }
    }

    @Override
    public ArtifactType getArtifactType() {

        return artifactType;
    }

    @Override
    public Artifact getArtifact() {

        return artifact;
    }

    @Override
    public List<String> getModuleNames() {

        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public boolean save() throws IOException {

        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public boolean undo() throws IOException {

        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public POM getParent() {

        return parent;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    void setArtifactType(ArtifactType t) {

        this.artifactType = t;
    }

    void setArtifact(Artifact a) {

        this.artifact = a;
    }

    void setFile(File file) {

        this.file = file;
    }

    void setGroupId(String s) {

        this.groupId = s;
    }

    void setParent(POM parent) {

        this.parent = parent;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
