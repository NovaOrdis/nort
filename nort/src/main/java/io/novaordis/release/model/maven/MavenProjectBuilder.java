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

import io.novaordis.release.model.Project;
import io.novaordis.release.model.ProjectBuilder;
import io.novaordis.utilities.UserErrorException;

import java.io.File;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/19/16
 */
public class MavenProjectBuilder implements ProjectBuilder {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String POM_FILE_NAME = "pom.xml";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // ProjectBuilder implementation ------------------------------------------------------------------------------

    @Override
    public Project build(File currentDirectory) throws Exception {

        //
        // figure out the release information source
        //

        File pomFile = new File(currentDirectory, POM_FILE_NAME);

        if (!pomFile.exists()) {

            throw new UserErrorException(POM_FILE_NAME + " not found in the current directory");
        }

        MavenProject mp;

        try {

            mp = new MavenProject(pomFile);
        }
        catch(Exception e) {

            throw new UserErrorException("failed to read POM file " + pomFile + ": " + e.getMessage(), e);
        }

        return mp;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
