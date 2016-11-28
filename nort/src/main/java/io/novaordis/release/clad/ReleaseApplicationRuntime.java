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
import io.novaordis.release.sequences.SequenceExecutionContext;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
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
    public void init(Configuration configuration) throws Exception {

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

        //
        // install the environment-dependent state
        //

        String s = System.getenv("M2");
        if (s == null) {
            throw new UserErrorException("M2 environment variable not defined, configure the location of the local Maven repository in the project configuration file");
        }

        configuration.set(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT, s);

        s = System.getenv("RUNTIME_DIR");
        if (s == null) {
            throw new UserErrorException("RUNTIME_DIR environment variable not defined, configure the location of the runtime directory in the project configuration file");
        }

        configuration.set(ConfigurationLabels.RUNTIME_DIRECTORY, s);

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
