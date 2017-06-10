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

package io.novaordis.release.sequences;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.model.Project;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class BuildSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BuildSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executeChangedState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BuildSequence() {

        this.executeChangedState = false;
    }

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext context) throws Exception {

        Configuration c = context.getConfiguration();
        ApplicationRuntime r = context.getRuntime();
        Project m = context.getProject();

        boolean executeTests = true;

        //
        // If the tests were executed during the qualification sequence and they passed, we don't want to execute them
        // again here, redundantly.
        //

        if (context.wereTestsExecuted()) {

            log.debug("test were executed already, won't execute here again");

            executeTests = false;

        }

        //
        // even if tests were not executed so far and normally we should, we won't if we were instructed not to
        //

        if ("true".equals(r.getVariableValue(ConfigurationLabels.QUALIFICATION_NO_TESTS))) {

            log.debug("we were configured not to execute tests, so we won't execute them here");

            executeTests = false;
        }

        String osBuildCommand;

        if (executeTests) {

            osBuildCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITH_TESTS);
        }
        else {

            osBuildCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS);
        }

        if (osBuildCommand == null) {
            throw new UserErrorException(
                    "the OS command to use to build " + (executeTests ? "with" : "without") +
                            " tests was not configured for this project");
        }

        Version currentVersion = m.getVersion();

        log.debug("building artifacts for release " + currentVersion + " ...");

        log.debug("building with \"" + osBuildCommand + "\" ...");

        NativeExecutionResult executionResult = OS.getInstance().execute(osBuildCommand);

        if (!executionResult.isSuccess()) {

            throw new UserErrorException("build failed");
        }

        executeChangedState = true;

        if (c.isVerbose()) {
            r.info(executionResult.getStdout());
        }

        r.info(currentVersion + " build ok");

        return executeChangedState;
    }

    @Override
    public boolean undo(SequenceExecutionContext context) {

        if (!executeChangedState) {

            // noop
            return false;
        }

        throw new RuntimeException("undo() NOT YET IMPLEMENTED");
    }

    @Override
    public boolean didExecuteChangeState() {

        return executeChangedState;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {

        return "build sequence";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
