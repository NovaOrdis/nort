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
import io.novaordis.release.version.VersionFormatException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * The project model.
 *
 * We don't use the Maven POM because we will build non-Maven and even non-Java projects.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/19/16
 */
public interface Project {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @return the project's name. For a simple single-pom Maven project, is the artifact ID. Different projects
     * may follow different conventions.
     */
    String getName();

    /**
     * The file the project metadata comes from.
     *
     * TODO: what if there are more then one?
     */
    File getFile();

    /**
     * The project's version. Depending on the project type, the version may be read from different locations. For
     * example, for a simple, single-module Maven project, the project version is the pom.xml version element.
     *
     * @exception VersionFormatException if the underlying project metadata storage contains an invalid version.
     */
    Version getVersion() throws VersionFormatException;

    /**
     * Modify the version information, as cached in memory. If multiple files must be modified to perform a
     * consistent version upgrade, the implementation takes care of that.
     *
     * In order to write the modification on disk, call save() on the Project instance.
     *
     * @param v the instance is already syntactically correct, as it was parsed.
     *
     * @see Project#save()
     *
     * @return true if the version information was changed in memory as result of the call, false otherwise (because
     * the cached version is the same as the version passed as argument).
     */
    boolean setVersion(Version v);

    File getBaseDirectory();

    /**
     * @return true if state was changed on disk as result of the save operation.
     *
     * @exception IOException if the underlying IO operation failed.
     */
    boolean save() throws IOException;

    /**
     * Reverts the effects of the last save command (if any), restoring the underlying file system stored state to
     * what it was before the last save command was applied.
     *
     * @exception IOException if the underlying IO operation failed.
     */
    boolean undo() throws IOException;

    /**
     * @return the type of release artifacts produced by this project.
     *
     * {@linktourl https://kb.novaordis.com/index.php/Nova_Ordis_Release_Tools_User_Manual_-_Concepts#Release_Artifacts}
     */
    Set<ArtifactType> getArtifactTypes();

    /**
     * Returns all artifacts produced by this project, in the order they were declared in the project descriptor. The
     * list is never empty, as a project must have at least one artifact.
     *
     * Consult the implementation to find out if the returned list is a copy of the internal state or the internal
     * storage.
     */
    List<Artifact> getArtifacts();

    /**
     * Returns the set of artifacts of the given type, in the order they were declared in the project descriptor. The
     * list may be empty, but never null.
     */
    List<Artifact> getArtifacts(ArtifactType artifactType);

}
