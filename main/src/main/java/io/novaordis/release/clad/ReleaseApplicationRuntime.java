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
import io.novaordis.clad.application.Console;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.Option;
import io.novaordis.clad.option.StringOption;
import io.novaordis.release.ZipHandler;
import io.novaordis.release.sequences.SequenceExecutionContext;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.variable.StringWithVariables;
import io.novaordis.utilities.variable.VariableProvider;
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

    public static final String DEFAULT_CONFIGURATION_FILE = "./.nort/project.yaml";

    // Static ----------------------------------------------------------------------------------------------------------

    // Package protected static ----------------------------------------------------------------------------------------

    /**
     * Do not fail here if specific configuration is missing. The Qualification Sequence will perform all checks. This
     * is because we don't know yet what specific configuration is required and what not. Fail on configuration file
     * parsing errors though.
     *
     * @param provider the provider to be used to resolve configuration file variables.
     *
     * @throws UserErrorException on configuration file parsing errors.
     */
    static void initializeEnvironmentRelatedConfiguration(
            Console console, Configuration configuration, VariableProvider provider) throws UserErrorException {

        //
        // TODO hackishly install the command to read the version of the already installed artifact and some other
        // configuration elements. Normally this should be done via a generic configuration file system, but clad does
        // not have that yet. Currently we rely on the -c <configuration-file> global variable.
        //

        String configurationFile;

        StringOption configurationFileOption = (StringOption)configuration.getGlobalOption(new StringOption('c'));

        if (configurationFileOption != null) {

            //
            // command line configuration file takes precedence
            //

            configurationFile = configurationFileOption.getString();
        }
        else {

            configurationFile = DEFAULT_CONFIGURATION_FILE;

            if (!new File(configurationFile).isFile()) {

                console.warn("default configuration file " + configurationFile + " not found");
                configurationFile = null;
            }

        }

        if (configurationFile != null) {
            loadConfiguration(configurationFile, configuration, provider);
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
        configuration.set(label, "mvn jar:jar source:jar install:install");
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
     * Loads relevant configuration from file into the configuration instance and perform as many validations as
     * possible, if a configuration element has a value. If a configuration element is missing, the layer that needs
     * it (QualificationSequence, etc.) will complain. This is because we don't know yet what specific configuration is
     * required and what not.
     *
     * @param path the configuration file path. Must exist, be readable and parseable, otherwise the method throws
     *             a UserErrorException.
     *
     * @param provider the VariableProvider implementation to use to resolve environment variables and other variables.
     *
     * @throws UserErrorException on configuration file parsing errors or on invalid values
     */
    static void loadConfiguration(String path, Configuration configuration, VariableProvider provider)
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

        Map qualificationMap = (Map) yamlFileConfiguration.get("qualification");
        extractString(
                qualificationMap, ConfigurationLabels.OS_COMMAND_TO_GET_INSTALLED_VERSION, provider, configuration);

        //
        // Publish Sequence Configuration
        //

        Map publishMap = (Map) yamlFileConfiguration.get("publish");
        extractDirectory(publishMap, ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, provider, configuration);

        //
        // Installation Sequence Configuration
        //

        Map installMap = (Map)yamlFileConfiguration.get("install");
        extractDirectory(installMap, ConfigurationLabels.INSTALLATION_DIRECTORY, provider, configuration);
    }

    /**
     * Attempts to extract a string, and if not null, resolves variables and then installs the result into the
     * configuration.
     *
     * @param map the corresponding configuration map. If null,the whole method is a noop.
     */
    static void extractString(Map map, String configKey, VariableProvider provider, Configuration c)
            throws UserErrorException {

        if (map == null) {

            return;
        }

        String s = (String) map.get(configKey);

        if (s == null) {

            log.debug("'" + configKey + "' not defined");
            return;
        }

        try {

            s = new StringWithVariables(s, true).resolve(provider);
        }
        catch(Exception e) {

            throw new UserErrorException(e);
        }

        c.set(configKey, s);
    }

    /**
     * Attempts to extract a string, and if not null, resolves variables, validates and then installs the result into
     * the configuration.
     *
     * @param map the corresponding configuration map. If null,the whole method is a noop.
     */
    static void extractDirectory(Map map, String configKey, VariableProvider provider, Configuration c)
            throws UserErrorException {

        if (map == null) {

            return;
        }

        String s = (String) map.get(configKey);

        if (s == null) {

            log.debug("'" + configKey + "' not defined");
            return;
        }

        try {

            s = new StringWithVariables(s, true).resolve(provider);
        }
        catch(Exception e) {

            throw new UserErrorException(e);
        }

        //
        // verify consistency
        //

        String normalizedPath = Files.normalizePath(s);

        File dir = new File(normalizedPath);

        if (!dir.isDirectory()) {

            throw new UserErrorException(
                    "'" + configKey + "' resolves to an invalid directory " +
                            Files.normalizePath(normalizedPath));
        }

        c.set(configKey, normalizedPath);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    private SequenceExecutionContext lastExecutionContext;

    private VariableProvider variableProvider;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ReleaseApplicationRuntime() {

        this.variableProvider = new NortVariableProvider();
    }

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

        super.init(configuration);

        initializeEnvironmentRelatedConfiguration(this, configuration, variableProvider);
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
