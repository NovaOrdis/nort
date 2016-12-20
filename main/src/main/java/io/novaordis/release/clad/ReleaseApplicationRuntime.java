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

import io.novaordis.clad.application.ApplicationRuntimeBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.Option;
import io.novaordis.clad.option.StringOption;
import io.novaordis.release.ZipHandler;
import io.novaordis.release.sequences.SequenceExecutionContext;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.zip.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The instance will be created by reflection.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
@SuppressWarnings("unused")
public class ReleaseApplicationRuntime extends ApplicationRuntimeBase {


    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ReleaseApplicationRuntime.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Package protected static ----------------------------------------------------------------------------------------

    /**
     * Do not fail here if specific configuration is missing. The Qualification Sequence will perform all checks. This
     * is because we don't know yet what specific configuration is required and what not. Fail on configuration file
     * parsing errors though.
     *
     * @throws UserErrorException on configuration file parsing errors.
     */
    static void initializeEnvironmentRelatedConfiguration(Configuration configuration) throws UserErrorException {

        //
        // TODO hackishly install the command to read the version of the already installed artifact and some other
        // configuration elements. Normally this should be done via a generic configuration file system, but clad does
        // not have that yet. Currently we rely on the -c <configuration-file> global variable.
        //

        StringOption configurationFileOption = (StringOption)configuration.getGlobalOption(new StringOption('c'));

        if (configurationFileOption != null) {

            loadConfiguration(configurationFileOption.getString(), configuration);
        }

        //
        // install the hardcoded defaults
        //

        String label = ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS;
        configuration.set(label, "mvn clean test");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_BUILD_WITH_TESTS;
        configuration.set(label, "mvn clean package");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS;
        configuration.set(label, "mvn -Dmaven.test.skip=true clean package");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_PUBLISH_INTO_LOCAL_REPOSITORY;
        configuration.set(label, "mvn jar:jar install:install");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_ADD_TO_LOCAL_SOURCE_REPOSITORY;
        configuration.set(label, "git add .");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_COMMIT_TO_LOCAL_SOURCE_REPOSITORY;
        configuration.set(label, "git commit -m \"release ${current_version}\"");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_TAG_LOCAL_SOURCE_REPOSITORY;
        configuration.set(label, "git tag -m \"release ${current_version}\" \"${tag}\"");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

        label = ConfigurationLabels.OS_COMMAND_TO_PUSH_TO_REMOTE_SOURCE_REPOSITORY;
        configuration.set(label, "git push --follow-tags");
        log.debug("set '" + label + "' to \"" + configuration.get(label) + "\"");

    }

    /**
     * Loads relevant configuration from file into the configuration instance.
     *
     * Do not fail here if specific configuration is missing. The Qualification Sequence will perform all checks. This
     * is because we don't know yet what specific configuration is required and what not. Fail on configuration file
     * parsing errors though.
     *
     * @param path the configuration file path. Must exist, be readable and parseable, otherwise the method throws
     *             a UserErrorException.
     *
     * @throws UserErrorException on configuration file parsing errors.
     */
    static void loadConfiguration(String path, Configuration configuration)
            throws UserErrorException {

        File configFile = new File(path);

        if (!configFile.isFile()) {

            throw new UserErrorException("configuration file " + Files.normalizePath(path) + " does not exist");
        }

        if (!configFile.canRead()) {

            throw new UserErrorException("configuration file " + Files.normalizePath(path) + " is not readable");
        }

        BufferedInputStream bis = null;
        Map<String, Object> yamlFileConfiguration = null;

        try {

            bis = new BufferedInputStream(new FileInputStream(configFile));

            Yaml yaml = new Yaml();

            //noinspection unchecked
            yamlFileConfiguration = (Map<String, Object>)yaml.load(bis);
        }
        catch(YAMLException e) {

            throw new UserErrorException(
                    "YAML file " + Files.normalizePath(path) + " parsing error: " + e.getMessage(), e);
        }
        catch(IOException e) {

            throw new UserErrorException(e);
        }
        finally {

            if (bis != null) {

                try {
                    bis.close();
                }
                catch(Exception e) {

                    log.warn("failed to close input stream", e);
                }
            }
        }

        //
        // Qualification Sequence Configuration
        //

        //noinspection unchecked
        Map qualificationConfig = (Map<String, Object>)yamlFileConfiguration.get("qualification");

        if (qualificationConfig != null) {

            String s = (String) qualificationConfig.get(ConfigurationLabels.OS_COMMAND_TO_GET_INSTALLED_VERSION);

            if (s != null) {

                configuration.set(ConfigurationLabels.OS_COMMAND_TO_GET_INSTALLED_VERSION, s);
            }
        }

        //
        // Publish Sequence Configuration
        //

        //noinspection unchecked
        Map publishConfig = (Map<String, Object>)yamlFileConfiguration.get("publish");

        if (publishConfig != null) {

            String s = (String) publishConfig.get(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT);

            if (s != null) {

                configuration.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, s);
            }
        }

        //
        // Installation Sequence Configuration
        //

        //noinspection unchecked
        Map installConfig = (Map<String, Object>)yamlFileConfiguration.get("install");

        if (installConfig != null) {

            String s = (String) installConfig.get(ConfigurationLabels.INSTALLATION_DIRECTORY);

            if (s != null) {

                configuration.set(ConfigurationLabels.INSTALLATION_DIRECTORY, s);
            }
        }

//        //
//        // install the environment-dependent state
//        //
//
//        EnvironmentVariableProvider evp = EnvironmentVariableProvider.getInstance();
//
//        String s = evp.getenv("M2");
//
//        if (s == null) {
//
//            throw new UserErrorException(
//                    "M2 environment variable not defined, configure the location of the local Maven repository in the project configuration file");
//        }
//
//        configuration.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, s);
//
//        s = evp.getenv("RUNTIME_DIR");
//
//        if (s == null) {
//
//            throw new UserErrorException(
//                    "RUNTIME_DIR environment variable not defined, configure the location of the runtime directory in the project configuration file");
//        }
//
//        configuration.set(ConfigurationLabels.INSTALLATION_DIRECTORY, s);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private SequenceExecutionContext lastExecutionContext;

    // Constructors ----------------------------------------------------------------------------------------------------

    // ApplicationRuntimeBase overrides --------------------------------------------------------------------------------

    @Override
    public String getDefaultCommandName() {
        return null;
    }

    @Override
    public Set<Option> requiredGlobalOptions() {

        return Collections.emptySet();
    }

    @Override
    public Set<Option> optionalGlobalOptions() {

        return Collections.emptySet();
    }

    @Override
    public void init(Configuration configuration) throws UserErrorException {

        initializeEnvironmentRelatedConfiguration(configuration);
    }

    /**
     * This is exposed for testing purposes only and it should not normally be used for anything else.
     */
    public SequenceExecutionContext getLastExecutionContext() {

        return lastExecutionContext;
    }

    /**
     * This is exposed for testing purposes only and it should not normally be used for anything else.
     */
    public void setLastExecutionContext(SequenceExecutionContext c) {

        this.lastExecutionContext = c;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public File getProjectHomeDirectory() {

        return new File(".");
    }

    /**
     * Pluggable logic that handles zips. It is pluggable to make tests portable.
     */
    public ZipHandler getZipHandler() {

        return ZipUtil::getTopLevelDirectoryName;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
