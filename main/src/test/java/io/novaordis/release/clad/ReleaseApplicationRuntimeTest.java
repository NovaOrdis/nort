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

import io.novaordis.clad.option.StringOption;
import io.novaordis.release.MockConfiguration;
import io.novaordis.release.Util;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.env.EnvironmentVariableProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void initializeEnvironmentRelatedConfiguration_ConfigurationFileParsingError() throws Exception {

        File f = new File(scratchDirectory, "invalid.yaml");
        assertTrue(Files.write(f,
                "a: b\n" +
                        "  c: d\n"));


        MockConfiguration mc = new MockConfiguration();

        StringOption so = new StringOption('c');
        so.setValue(f.getPath());
        mc.addGlobalOption(so);

        try {

            ReleaseApplicationRuntime.initializeEnvironmentRelatedConfiguration(mc);
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

    // loadConfiguration() ---------------------------------------------------------------------------------------------


    @Test
    public void loadConfiguration_FileDoesNotExist() throws Exception {

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration("/I/am/sure/this/file/does/not/exist.yaml", mc);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("configuration file .* does not exist"));
        }
    }

    @Test
    public void loadConfiguration_FileNotReadable() throws Exception {

        File f = new File(scratchDirectory, "cannot-read.yaml");
        assertTrue(Files.write(f, "something"));
        assertTrue(Files.chmod(f, "-w--w--w-"));
        assertFalse(f.canRead());

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(f.getPath(), mc);
            fail("should have thrown exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertTrue(msg.matches("configuration file .* is not readable"));
        }
    }

    @Test
    public void loadConfiguration_ParsingError() throws Exception {

        File f = new File(scratchDirectory, "invalid.yaml");
        assertTrue(Files.write(f,
                "a: b\n" +
                        "  c: d\n"));

        MockConfiguration mc = new MockConfiguration();

        try {

            ReleaseApplicationRuntime.loadConfiguration(f.getPath(), mc);
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
    public void processConfigurationFile_ReferenceConfigurationFile() throws Exception {

        File config = Util.cp("configuration/reference.yaml", scratchDirectory);

        //
        // one environment variable is defined, the other one is not
        //

        fail("RETURN HERE");
        // set("M2", "/mock/repository");

        //
        // "RUNTIME_DIR" is not defined
        //

        MockConfiguration mc = new MockConfiguration();

        ReleaseApplicationRuntime.loadConfiguration(config.getPath(), mc);

        String value;

        value = mc.get(ConfigurationLabels.OS_COMMAND_TO_GET_INSTALLED_VERSION);
        assertEquals("some-command", value);

        value = mc.get(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT);
        assertEquals("/mock/repository", value);

        value = mc.get(ConfigurationLabels.INSTALLATION_DIRECTORY);
        assertEquals("${RUNTIME_DIR}", value);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
