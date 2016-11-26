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

import io.novaordis.utilities.Files;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/25/16
 */
public class Util {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * Fails if the file/directory is not found or a copy failure occurs.
     *
     * Works on both files and directories (recursively).
     *
     * @return the destination File
     */
    public static File cp(
            File baseDirectory, String srcPathRelativeToBaseDir,
            File scratchDirectory, String destPathRelativeToScratchDir) {

        File orig = new File(baseDirectory, srcPathRelativeToBaseDir);
        assertTrue(orig.isFile() || orig.isDirectory());

        File destFile = scratchDirectory;
        if (destPathRelativeToScratchDir != null) {

            destFile = new File(scratchDirectory, destPathRelativeToScratchDir);
        }

        assertTrue(Files.cp(orig, destFile));
        return destFile.isFile() ? destFile : new File(destFile, orig.getName());
    }

    public static File cp(File baseDirectory, String srcPathRelativeToBaseDir, File scratchDirectory) {

        return cp(baseDirectory, srcPathRelativeToBaseDir, scratchDirectory, null);
    }

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
