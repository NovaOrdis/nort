/*
 * Copyright (c) 2018 Nova Ordis LLC
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

package io.novaordis.release.clad.configuration;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.novaordis.release.MockConfiguration;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.expressions.Scope;
import io.novaordis.utilities.expressions.ScopeImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/20/18
 */
public class TruststoreTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    protected File scratchDirectory;

    @Before
    public void setup() {

        String projectBaseDirName = System.getProperty("basedir");
        scratchDirectory = new File(projectBaseDirName, "target/test-scratch");
        assertTrue(scratchDirectory.isDirectory());
    }

    @After
    public void cleanup() {

        assertTrue(io.novaordis.utilities.Files.rmdir(scratchDirectory, false));
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    // constructors ----------------------------------------------------------------------------------------------------

    @Test
    public void constructor_AbsolutePath() throws Exception {

        File f = new File(scratchDirectory, "mock.truststore");
        Files.createFile(f.toPath());
        assertTrue(f.isFile());

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, f.getAbsolutePath());
        map.put(Truststore.PASSWORD_KEY, "something");

        Truststore t = new Truststore(map, null);

        assertEquals(f, t.getFile());
        assertEquals("something", t.getPassword());
    }

    @Test
    public void constructor_RelativePath() throws Exception {

        File f = new File(scratchDirectory, "mock.truststore");
        Files.createFile(f.toPath());
        assertTrue(f.isFile());

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, "mock.truststore");
        map.put(Truststore.PASSWORD_KEY, "something");

        Truststore t = new Truststore(map, scratchDirectory);

        assertEquals(new File(scratchDirectory, "mock.truststore"), t.getFile());
        assertEquals("something", t.getPassword());
    }

    @Test
    public void constructor_FileDoesNotExist() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, "/a/file/${THAT}/does/not/exists");
        map.put(Truststore.PASSWORD_KEY, "something");

        Truststore t = new Truststore(map, null);

        //
        // even if the file does not exist, we'll return as read, because it may contain environment variable reference
        //

        assertEquals(new File("/a/file/${THAT}/does/not/exists"), t.getFile());
    }

    // toConfiguration() -----------------------------------------------------------------------------------------------

    @Test
    public void toConfiguration_NoEnvironmentVariables() throws Exception {

        File f = new File(scratchDirectory, "mock.truststore");
        Files.createFile(f.toPath());
        assertTrue(f.isFile());

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, "mock.truststore");
        map.put(Truststore.PASSWORD_KEY, "something");

        Truststore t = new Truststore(map, scratchDirectory);

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();

        t.toConfiguration(mc, scope);

        String s = mc.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE);
        assertEquals(new File(scratchDirectory, "mock.truststore").getAbsolutePath(), s);

        String s2 = mc.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD);
        assertEquals("something", s2);
    }

    @Test
    public void toConfiguration_WithEnvironmentVariables() throws Exception {

        File f = new File(scratchDirectory, "mock.truststore");
        Files.createFile(f.toPath());
        assertTrue(f.isFile());

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, "${TRUSTSTORE_FILE}");
        map.put(Truststore.PASSWORD_KEY, "${PASSWORD}");

        Truststore t = new Truststore(map, scratchDirectory);

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();
        scope.declare("TRUSTSTORE_FILE", "mock.truststore");
        scope.declare("PASSWORD", "something");

        t.toConfiguration(mc, scope);

        String s = mc.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE);
        assertEquals(new File(scratchDirectory, "mock.truststore"), new File(s));

        String s2 = mc.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD);
        assertEquals("something", s2);
    }

    @Test
    public void toConfiguration_FileDoesNotExist() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, "/this/is/not/a/real/file");
        map.put(Truststore.PASSWORD_KEY, "something");

        Truststore t = new Truststore(map, null);

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();

        try {

            t.toConfiguration(mc, scope);

            fail("should have thrown exception");

        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("/this/is/not/a/real/file"));
            assertTrue(msg.contains("does not exist or it is not a file"));
        }
    }

    @Test
    public void toConfiguration_FileIsNotAFile() throws Exception {

        File f = new File(scratchDirectory, "test.dir");
        assertTrue(f.mkdir());
        assertTrue(f.isDirectory());

        Map<String, String> map = new HashMap<>();
        map.put(Truststore.FILE_KEY, "test.dir");
        map.put(Truststore.PASSWORD_KEY, "something");

        Truststore t = new Truststore(map, scratchDirectory);

        MockConfiguration mc = new MockConfiguration();
        Scope scope = new ScopeImpl();

        try {

            t.toConfiguration(mc, scope);

            fail("should have thrown exception");

        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("test.dir"));
            assertTrue(msg.contains("does not exist or it is not a file"));
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
