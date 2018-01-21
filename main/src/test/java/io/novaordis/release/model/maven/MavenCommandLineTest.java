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

package io.novaordis.release.model.maven;

import org.junit.Test;

import io.novaordis.release.MockConfiguration;
import io.novaordis.release.clad.ConfigurationLabels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/20/18
 */
public class MavenCommandLineTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    // constructor -----------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        MavenCommandLine mvnCL = new MavenCommandLine("jar:jar", "source:jar", "install:install");

        assertEquals("mvn jar:jar source:jar install:install", mvnCL.getCommandLine());
    }

    // append() --------------------------------------------------------------------------------------------------------

    @Test
    public void append() throws Exception {

        MavenCommandLine mvnCL = new MavenCommandLine("jar:jar", "source:jar", "install:install");

        mvnCL.append("deploy:deploy");

        assertEquals("mvn jar:jar source:jar install:install deploy:deploy", mvnCL.getCommandLine());
    }

    // configureLocalTruststore() --------------------------------------------------------------------------------------

    @Test
    public void configureLocalTruststore_NoTruststoreConfiguration() throws Exception {

        MavenCommandLine mvnCL = new MavenCommandLine("clean");

        MockConfiguration mc = new MockConfiguration();

        assertNull(mc.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE));
        assertNull(mc.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD));

        mvnCL.configureLocalTruststore(mc);

        assertEquals("mvn clean", mvnCL.getCommandLine());
    }

    @Test
    public void configureLocalTruststore_TruststoreConfigurationExists() throws Exception {

        MavenCommandLine mvnCL = new MavenCommandLine("clean");

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE, "/some/truststore/file");
        mc.set(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD, "some-password");

        mvnCL.configureLocalTruststore(mc);

        assertEquals(
                "mvn -Djavax.net.ssl.trustStore=/some/truststore/file -Djavax.net.ssl.trustStorePassword=some-password clean",
                mvnCL.getCommandLine());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
