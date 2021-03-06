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

import io.novaordis.release.model.maven.MavenProject;
import io.novaordis.release.model.maven.POM;
import io.novaordis.release.model.maven.ProjectVersioningModel;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class MockMavenProject extends MavenProject {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ProjectVersioningModel versioningModel;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockMavenProject() throws Exception {
        super();

        //
        // default behavior
        //
        this.versioningModel = ProjectVersioningModel.MULTIPLE_MODULE_LOCKSTEP;
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    @Override
    public ProjectVersioningModel getVersioningModel() {

        return versioningModel;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setPOM(POM pom) {

        super.setPOM(pom);
    }

    public void setVersioningModel(ProjectVersioningModel versioningModel) {

        this.versioningModel = versioningModel;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
