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
import java.util.Map;

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.expressions.Scope;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 1/20/18
 */
public class Truststore {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String FILE_KEY = "file";
    public static final String PASSWORD_KEY = "password";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File file;
    private String password;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * The map extracted from a YAML representation. We expect it to contain "file", "password".
     *
     * @param configurationDirectory needed when the truststore file is specified with a relative path. May be
     *                                   null if the truststore file path is absolute.
     *
     * @exception UserErrorException if the file is not specified.
     */
    public Truststore(Map yamlMap, File configurationDirectory) throws UserErrorException {

        if (yamlMap == null) {

            throw new IllegalArgumentException("null map");
        }

        Object o = yamlMap.get(FILE_KEY);

        if (o == null) {

            throw new UserErrorException("no truststore file specified");
        }

        if (!(o instanceof String)) {

            throw new UserErrorException("the truststore file value should be a string, but it is " + o.getClass());
        }

        File f = new File((String)o);

        if (!f.isAbsolute()) {

            if (configurationDirectory == null) {

                throw new IllegalArgumentException("null configuration directory");
            }

            if (!configurationDirectory.isDirectory()) {

                throw new UserErrorException("configuration directory " + configurationDirectory +
                        " does not exist or is not a directory");
            }

            f = new File(configurationDirectory, (String)o);
        }
        else {

            f = new File((String)o);
        }

        this.file = f;

        o = yamlMap.get(PASSWORD_KEY);

        if (o == null) {

            throw new UserErrorException("no truststore password specified");
        }

        if (!(o instanceof String)) {

            throw new UserErrorException("the truststore password value should be a string, but it is " + o.getClass());
        }

        this.password = (String)o;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Resolves in scope and transfers the file and password values to the configuration.
     *
     * @throws UserErrorException if the truststore file does not exist on disk, or it is not a file
     */
    public void toConfiguration(Configuration configuration, Scope scope) throws UserErrorException {

        String resolvedPath = scope.evaluate(file.getPath(), false);

        File resolvedFile = new File(resolvedPath);

        if (!resolvedFile.isFile() || !resolvedFile.exists()) {

            throw new UserErrorException(resolvedFile + " does not exist or it is not a file");
        }

        String resolvedPassword = scope.evaluate(password, false);

        configuration.set(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_FILE, resolvedFile.getAbsolutePath());
        configuration.set(ConfigurationLabels.INTERNAL_KEY_TRUSTSTORE_PASSWORD, resolvedPassword);
    }

    /**
     * @return the truststore file. May not be null. The file representation may contain unresolved environment
     * variable references.
     */
    public File getFile() {

        return file;
    }

    /**
     * @return the truststore file. May not be null. The password representation may contain unresolved environment
     * variable references.
     */
    public String getPassword() {

        return password;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
