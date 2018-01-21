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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.YAMLException;

import io.novaordis.clad.option.StringOption;
import io.novaordis.release.MockConfiguration;
import io.novaordis.release.Util;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.expressions.Scope;
import io.novaordis.utilities.expressions.ScopeImpl;
import io.novaordis.utilities.expressions.env.EnvironmentVariableProvider;

import static io.novaordis.release.clad.ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE;
import static io.novaordis.release.clad.ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 12/20/16
 */
public class ReleaseApplicationRuntimeTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ReleaseApplicationRuntime.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    protected File scratchDirectory;
    @SuppressWarnings("WeakerAccess")
    protected File baseDirectory;

    @Before
    public void setup() {

        System.setProperty(
                EnvironmentVariableProvider.ENVIRONMENT_VARIABLE_PROVIDER_CLASS_NAME_SYSTEM_PROPERTY,
                MockEnvironmentVariableProvider.class.getName());

        EnvironmentVariableProvider.reset();

        String projectBaseDirName = System.getProperty("basedir");
        scratchDirectory = new File(projectBaseDirName, "target/test-scratch");
        assertTrue(scratchDirectory.isDirectory());

        baseDirectory = new File(System.getProperty("basedir"));
        assertTrue(baseDirectory.isDirectory());
    }

    @After
    public void cleanup() {

        System.clearProperty(EnvironmentVariableProvider.ENVIRONMENT_VARIABLE_PROVIDER_CLASS_NAME_SYSTEM_PROPERTY);
        EnvironmentVariableProvider.reset();

        //
        // scratch directory cleanup
        //

        assertTrue(io.novaordis.utilities.Files.rmdir(scratchDirectory, false));
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    // initializeEnvironmentRelatedConfiguration() ---------------------------------------------------------------------

    @Test
    public void initializeEnvironmentRelatedConfiguration_ConfigurationFileParsingError() {

        File f = new File(scratchDirectory, "invalid.yaml");
        assertTrue(Files.write(f,
                "a: b\n" +
                        "  c: d\n"));

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();

        StringOption so = new StringOption('c');
        so.setValue(f.getPath());
        mc.addGlobalOption(so);

        try {

            ReleaseApplicationRuntime.initializeEnvironmentRelatedConfiguration(null, mc, scope);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            int i = msg.indexOf('\n');
            i = i == -1 ? msg.length() : i;
            msg = msg.substring(0, i);
            assertTrue(msg.matches("YAML file .* parsing error: .*"));

            Throwable cause = e.getCause();
            assertTrue(cause instanceof YAMLException);
        }
    }

    // locateDefaultConfigurationFile() --------------------------------------------------------------------------------

    @Test
    public void locateDefaultConfigurationFile_NoDefaultConfigFile() {

        String originalContent = ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_DIRECTORY;

        try {

            File d = new File(scratchDirectory, "mock-config");
            assertTrue(d.mkdir());

            ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_DIRECTORY = d.getPath();

            MockConsole mc = new MockConsole();
            File f = ReleaseApplicationRuntime.locateDefaultConfigurationFile(mc);
            assertNull(f);

            String w = mc.getWarningContent();
            log.info(w);
        }
        finally {

            ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_DIRECTORY = originalContent;
        }
    }

    @Test
    public void locateDefaultConfigurationFile() {

        String originalContent = ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_DIRECTORY;

        try {

            File d = new File(scratchDirectory, "mock-config");
            assertTrue(d.mkdir());
            ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_DIRECTORY = d.getPath();

            File f = new File(d, ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_FILE_NAME + ".yml");
            assertTrue(Files.write(f, "..."));

            MockConsole mc = new MockConsole();
            File f2 = ReleaseApplicationRuntime.locateDefaultConfigurationFile(mc);
            assertEquals(f, f2);

            String w = mc.getWarningContent();
            assertNull(w);
        }
        finally {

            ReleaseApplicationRuntime.DEFAULT_CONFIGURATION_DIRECTORY = originalContent;
        }
    }

    // loadConfiguration() ---------------------------------------------------------------------------------------------

    @Test
    public void loadConfiguration_FileDoesNotExist() {

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(new File("/I/am/sure/this/file/does/not/exist.yaml"), mc, null);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("configuration file .* does not exist"));
        }
    }

    @Test
    public void loadConfiguration_FileNotReadable() {

        File f = new File(scratchDirectory, "cannot-read.yaml");
        assertTrue(Files.write(f, "something"));
        assertTrue(Files.chmod(f, "-w--w--w-"));
        assertFalse(f.canRead());

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(f, mc, null);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("configuration file .* is not readable"));
        }
    }

    @Test
    public void loadConfiguration_ParsingError() {

        File f = new File(scratchDirectory, "invalid.yaml");
        assertTrue(Files.write(f,
                "a: b\n" +
                        "  c: d\n"));

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(f, mc, null);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);

            int i = msg.indexOf('\n');
            i = i == -1 ? msg.length() : i;
            msg = msg.substring(0, i);
            assertTrue(msg.matches("YAML file .* parsing error: .*"));
        }
    }

    @Test
    public void loadConfiguration_FileIsEmpty() throws Exception {

        File config = new File(scratchDirectory, "test.yml");
        Files.write(config, "");
        assertTrue(config.isFile());

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();
        ReleaseApplicationRuntime.loadConfiguration(config, mc, scope);

        assertNull(mc.get("something"));
    }

    @Test
    public void loadConfiguration_localArtifactRepositoryRootVariableNotDefined() {

        File config = Util.cp("configuration/reference.yaml", scratchDirectory);

        File localRepositoryDirectory = new File(scratchDirectory, "mock-repository-directory");

        assertTrue(localRepositoryDirectory.mkdir());

        File installationDirectory = new File(scratchDirectory, "mock-installation-directory");

        assertTrue(installationDirectory.mkdir());

        Scope scope = new ScopeImpl();

        //
        // M2 variable not defined
        //

        assertNull(scope.getVariable("M2"));

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(config, mc, scope);

            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("M2"));
            assertTrue(msg.contains("cannot be resolved"));
        }
    }

    @Test
    public void loadConfiguration_localArtifactRepositoryRootVariableInvalidValue() {

        File config = Util.cp("configuration/reference.yaml", scratchDirectory);

        File localRepositoryDirectory = new File(scratchDirectory, "mock-repository-directory");
        assertTrue(localRepositoryDirectory.mkdir());

        File installationDirectory = new File(scratchDirectory, "mock-installation-directory");
        assertTrue(installationDirectory.mkdir());

        Scope scope = new ScopeImpl();

        //
        // M2 variable has an invalid value
        //

        scope.declare("M2", "/I/am/pretty/sure/there/is/no/such/directory");

        MockConfiguration mc = new MockConfiguration();

        try {
            ReleaseApplicationRuntime.loadConfiguration(config, mc, scope);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches(
                    "'" + ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT + "' resolves to an invalid directory .*"));
        }
    }

    @Test
    public void loadConfiguration_installationDirectoryVariableNotDefined() {

        File config = Util.cp("configuration/reference.yaml", scratchDirectory);

        File localRepositoryDirectory = new File(scratchDirectory, "mock-repository-directory");
        assertTrue(localRepositoryDirectory.mkdir());

        File installationDirectory = new File(scratchDirectory, "mock-installation-directory");
        assertTrue(installationDirectory.mkdir());

        File truststoreFile = new File(scratchDirectory, "mock.truststore");
        assertTrue(Files.write(truststoreFile, "."));
        assertTrue(truststoreFile.isFile());

        Scope scope = new ScopeImpl();

        scope.declare("M2", localRepositoryDirectory.getPath());
        scope.declare("TRUSTSTORE_PASSWORD", "something");

        //
        // RUNTIME_DIR variable not defined
        //

        assertNull(scope.getVariable("RUNTIME_DIR"));

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(config, mc, scope);

            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("RUNTIME_DIR"));
            assertTrue(msg.contains("cannot be resolved"));
        }
    }

    @Test
    public void loadConfiguration_TruststoreNotAMap() {

        File config = new File(scratchDirectory, "test.yml");
        Files.write(config, "publish:\n  truststore: blah\n");
        assertTrue(config.isFile());

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();

        try {

            ReleaseApplicationRuntime.loadConfiguration(config, mc, scope);

            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("truststore declaration is not a map"));
        }
    }

    @Test
    public void loadConfiguration_ReferenceConfigurationFile() throws Exception {

        File config = Util.cp("configuration/reference.yaml", scratchDirectory);

        File localRepositoryDirectory = new File(scratchDirectory, "mock-repository-directory");
        assertTrue(localRepositoryDirectory.mkdir());

        File installationDirectory = new File(scratchDirectory, "mock-installation-directory");
        assertTrue(installationDirectory.mkdir());

        File truststoreCopy = Util.cp("configuration/mock.truststore", scratchDirectory);
        assertTrue(truststoreCopy.isFile());

        Scope scope = new ScopeImpl();

        scope.declare("M2", localRepositoryDirectory.getPath());
        scope.declare("RUNTIME_DIR", installationDirectory.getPath());
        scope.declare("TRUSTSTORE_PASSWORD", "mock_truststore_passwd");

        MockConfiguration mc = new MockConfiguration();

        ReleaseApplicationRuntime.loadConfiguration(config, mc, scope);

        String value;

        value = mc.get(ConfigurationLabels.OS_COMMAND_TO_GET_INSTALLED_VERSION);
        assertEquals("some-command", value);

        value = mc.get(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT);
        assertEquals(localRepositoryDirectory.getPath(), value);

        value = mc.get(ConfigurationLabels.RELEASE_TAG);
        assertEquals("release-some-marker-${current.version}", value);

        value = mc.get(ConfigurationLabels.INSTALLATION_DIRECTORY);
        assertEquals(installationDirectory.getPath(), value);

        String truststoreFilePath = mc.get(INTERNAL_KEY_TRUSTSTORE_FILE);
        String normalizedTruststoreFilePath = Files.normalizePath(truststoreFilePath);
        assertEquals(truststoreCopy.getAbsolutePath(), normalizedTruststoreFilePath);
        assertEquals("mock_truststore_passwd", mc.get(INTERNAL_KEY_TRUSTSTORE_PASSWORD));
    }

    // extractString() -------------------------------------------------------------------------------------------------

    @Test
    public void extractString_FailOnUnresolvedVariable_VariableCanBeResolved() throws Exception {

        String configKey = "a";

        Map<String, Object> map = new HashMap<>();
        map.put(configKey, "${b}");
        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();
        scope.declare("b", "B");

        ReleaseApplicationRuntime.extractString(map, configKey, scope, mc, true);

        String s = mc.get(configKey);
        assertEquals("B", s);
    }

    @Test
    public void extractString_FailOnUnresolvedVariable_VariableCannotBeResolved() throws Exception {

        String configKey = "a";

        Map<String, Object> map = new HashMap<>();
        map.put(configKey, "${b}");
        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();


        try {

            ReleaseApplicationRuntime.extractString(map, configKey, scope, mc, true);

            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("'b'"));
            assertTrue(msg.contains("cannot be resolved"));
        }

        String s = mc.get(configKey);
        assertNull(s);
    }

    @Test
    public void extractString_DoNotFailOnUnresolvedVariable_VariableCanBeResolved() throws Exception {

        String configKey = "a";

        Map<String, Object> map = new HashMap<>();
        map.put(configKey, "${b}");
        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();
        scope.declare("b", "B");

        ReleaseApplicationRuntime.extractString(map, configKey, scope, mc, false);

        String s = mc.get(configKey);
        assertEquals("B", s);
    }

    @Test
    public void extractString_DoNotFailOnUnresolvedVariable_VariableCannotBeResolved() throws Exception {

        String configKey = "a";

        Map<String, Object> map = new HashMap<>();
        map.put(configKey, "${b}");
        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();

        ReleaseApplicationRuntime.extractString(map, configKey, scope, mc, false);

        String s = mc.get(configKey);
        assertEquals("${b}", s);
    }

    // extractDirectory() ----------------------------------------------------------------------------------------------

    @Test
    public void extractDirectory() throws Exception {

        File someDir = new File(scratchDirectory, "mock-directory");
        assertTrue(someDir.mkdir());

        String configKey = "a";

        Map<String, Object> map = new HashMap<>();
        map.put(configKey, "${b}");
        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();
        scope.declare("b", someDir.getPath());

        ReleaseApplicationRuntime.extractDirectory(map, configKey, scope, mc);

        String s = mc.get(configKey);
        assertEquals(someDir.getPath(), s);
    }

    @Test
    public void extractDirectory_InvalidValue() throws Exception {

        String configKey = "a";

        Map<String, Object> map = new HashMap<>();
        map.put(configKey, "${b}");
        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();
        scope.declare("b", "/I/am/sure/this/directory/does/not/exist");

        try {
            ReleaseApplicationRuntime.extractDirectory(map, configKey, scope, mc);
            fail("should throw exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("'a' resolves to an invalid directory /I/am/sure/this/directory/does/not/exist", msg);
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
