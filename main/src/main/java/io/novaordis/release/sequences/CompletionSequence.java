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

import io.novaordis.release.ReleaseMode;
import io.novaordis.release.model.Project;
import io.novaordis.release.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class CompletionSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(CompletionSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executeChangedState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public CompletionSequence() {

        this.executeChangedState = false;
    }

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext context) throws Exception {

        Project p = context.getProject();
        ReleaseMode rm = context.getReleaseMode();

        Version currentVersion = p.getVersion();

        if ((rm.isSnapshot() && currentVersion.isDot()) || (rm.isDot() && currentVersion.isSnapshot())) {

            throw new IllegalStateException(
                    "current version cannot be a " + (currentVersion.isSnapshot() ? "snapshot" : "dot") +
                            " version (" + currentVersion + ") for a " + (rm.isSnapshot() ? "snapshot" : "dot") +
                            " release");
        }

        //
        // we always increment to the next snapshot
        //
        Version nextVersion = Version.nextVersion(currentVersion, ReleaseMode.snapshot);

        log.debug("incrementing release metadata to the next " + rm + " version " + nextVersion + " in " +
                p.getBaseDirectory().getAbsolutePath() + " workarea ...");

        p.setVersion(nextVersion);
        executeChangedState = p.save() || executeChangedState;

        log.debug("version " + nextVersion + " written to " + p.getFile());

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

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
