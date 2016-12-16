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

package io.novaordis.release;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.utilities.os.NativeExecutionResult;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/22/16
 */
public class OutputUtil {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    public static NativeExecutionResult handleNativeCommandOutput(
            NativeExecutionResult r, ApplicationRuntime runtime, Configuration c) {

        if (!r.isSuccess() || c.isVerbose()) {

            //
            // report stderr and stdout on failure, it'll help us diagnose, or in verbose mode
            //

            String stdout = r.getStdout();
            if (stdout != null) {
                runtime.info(stdout);
            }

            String stderr = r.getStderr();
            if (stderr != null) {
                runtime.info(stderr);
            }
        }

        return r;
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    private OutputUtil() {
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
