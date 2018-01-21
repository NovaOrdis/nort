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

package io.novaordis.release.clad;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class ConfigurationLabels {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    public static final String OS_COMMAND_TO_EXECUTE_ALL_TESTS = "os.command.to.execute.all.tests";
    public static final String OS_COMMAND_TO_BUILD_WITH_TESTS = "os.command.to.build.with.tests";
    public static final String OS_COMMAND_TO_BUILD_WITHOUT_TESTS = "os.command.to.build.without.tests";

    public static final String OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY = "os.command.to.add.to.local.source.repository";
    public static final String OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY = "os.command.to.commit.to.local.source.repository";

    public static final String OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY = "os.command.to.tag.local.source.repository";
    public static final String OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY = "os.command.to.push.to.remote.source.repository";

    public static final String LOCAL_ARTIFACT_REPOSITORY_ROOT = "local.artifact.repository.root";

    public static final String RELEASE_TAG = "release.tag";

    public static final String OS_COMMAND_TO_GET_INSTALLED_VERSION = "os.command.to.get.installed.version";

    public static final String INSTALLATION_DIRECTORY = "installation.directory";

    public static final String TRUSTSTORE = "truststore";

    //
    // flattened configuration keys
    //

    public static final String INTERNAL_KEY_TRUSTSTORE_FILE = "internal.key.truststore.file";
    public static final String INTERNAL_KEY_TRUSTSTORE_PASSWORD = "internal.key.truststore.password";

    //
    // temporarily using this for runtime variable names, this needs to be refactored anyway
    //

    //
    // TODO properly handle these
    //

    public static final String CURRENT_VERSION = "current.version";

    public static final String QUALIFICATION_NO_TESTS = "qualification.no.tests";
    public static final String PUBLISH_NO_PUSH = "publish.no.push";
    public static final String INSTALL_NO_INSTALL = "install.no.install";

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
