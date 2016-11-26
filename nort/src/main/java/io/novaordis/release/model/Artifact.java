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

import java.io.File;

/**
 * A project's artifact.
 *
 * A project may have multiple artifacts, of multiple types.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public interface Artifact {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @see ArtifactType
     */
    ArtifactType getType();

    /**
     * @return the file associated with this artifact, relative to the <b>root</b> of the local artifact repository.
     * The root of the repository is not present in the path of the file. The file path is always relative.
     * After fully resolving it relative to the repository, the corresponding file may not exist on disk, depending
     * on whether the project built and published the artifact.
     */
    File getRepositoryFile();

    boolean equals(Object o);

    int hashCode();
}
