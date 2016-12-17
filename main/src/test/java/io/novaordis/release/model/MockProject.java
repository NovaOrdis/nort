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

import io.novaordis.release.model.maven.MockMavenArtifact;
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/19/16
 */
public class MockProject implements Project {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String name;

    private Version currentVersion;

    private List<Version> savedVersionHistory;

    protected Map<ArtifactType, List<Artifact>> artifacts;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param currentVersion the project's current version.
     */
    public MockProject(String currentVersion) throws VersionFormatException {

        this(new Version(currentVersion));
    }

    /**
     * @param currentVersion the project's current version.
     */
    public MockProject(Version currentVersion) throws VersionFormatException {

        setVersion(currentVersion);
        this.savedVersionHistory = new ArrayList<>();
        this.artifacts = new HashMap<>();
        this.name = "mock";
    }

    // Project implementation -------------------------------------------------------------------------------------

    @Override
    public String getName() {

        return name;
    }

    @Override
    public File getFile() {

        return new File("MOCK-PROJECT-MODEL.txt");
    }

    @Override
    public Version getVersion() {

        return currentVersion;
    }

    @Override
    public boolean setVersion(Version v) {

        boolean changed =
                (currentVersion != null && !currentVersion.equals(v)) || (currentVersion == null && v != null);

        this.currentVersion = v;

        return changed;
    }

    @Override
    public File getBaseDirectory() {

        return new File(".");
    }

    @Override
    public boolean save() throws IOException {

        Version lastSavedVersion = getLastSavedVersion();

        boolean stateChanged = lastSavedVersion == null || !currentVersion.equals(lastSavedVersion);

        if (stateChanged) {
            savedVersionHistory.add(currentVersion);
        }

        return stateChanged;
    }

    @Override
    public boolean undo() throws IOException {
        throw new RuntimeException("undo() NOT YET IMPLEMENTED");
    }

    @Override
    public Set<ArtifactType> getArtifactTypes() {
        return artifacts.keySet();
    }

    @Override
    public List<Artifact> getArtifacts() {

        List<Artifact> result = new ArrayList<>();

        for(List<Artifact> as: artifacts.values()) {
            result.addAll(as);
        }

        return result;
    }

    @Override
    public List<Artifact> getArtifacts(ArtifactType artifactType) {

        return artifacts.get(artifactType);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * The last version physically "saved" - may return null.
     */
    public Version getLastSavedVersion() {

        if (savedVersionHistory.isEmpty()) {
            return null;
        }

        return savedVersionHistory.get(savedVersionHistory.size() - 1);
    }

    public List<Version> getSavedVersionHistory() {

        return savedVersionHistory;
    }

    public void addArtifact(ArtifactType type, File remoteFile, File localFile) {

        List<Artifact> as = artifacts.get(type);

        if (as == null) {

            as = new ArrayList<>();
            artifacts.put(type, as);
        }

        as.add(new MockMavenArtifact(type, remoteFile, localFile));
    }

    public void setName(String name) {

        this.name = name;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
