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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.clad.ConfigurationLabels;

/**
 * Represents a maven command line. Example:
 *
 * "mvn clean install"
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/20/18
 */
public class MavenCommandLine {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<SystemProperty> systemProperties;
    private List<String> arguments;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MavenCommandLine(String... commandLineArguments) {

        this.arguments = new ArrayList<>();
        this.systemProperties = new ArrayList<>();
        arguments.addAll(Arrays.asList(commandLineArguments));
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void append(String commandLineFragment) {

        if (commandLineFragment == null) {

            throw new IllegalArgumentException("null command line fragment");
        }

        arguments.add(commandLineFragment.trim());
    }

    public String getCommandLine() {

        String s = "mvn";

        for (SystemProperty systemProperty : systemProperties) {

            s += " ";
            s += systemProperty.toCommandLine();
        }

        for (String argument : arguments) {

            s += " ";
            s += argument;
        }

        return s;
    }

    /**
     * Deploying externally may require setting a local truststore on command line if the remote repository has a
     * self-signed certificate. It only does that if the configuration contains truststore information.
     */
    public void configureLocalTruststore(Configuration c) {

        String s = c.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE);

        if (s == null) {

            //
            // no truststore file configuration, return
            //

            return;
        }

        systemProperties.add(new SystemProperty("javax.net.ssl.trustStore", s));

        s = c.get(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD);

        if (s != null) {

            systemProperties.add(new SystemProperty("javax.net.ssl.trustStorePassword", s));
        }
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
