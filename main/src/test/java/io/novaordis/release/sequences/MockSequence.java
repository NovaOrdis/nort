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

import io.novaordis.utilities.NotYetImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/18/16
 */
public class MockSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockSequence.class);


    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executionBroken;
    private boolean undoBroken;
    private boolean executeInvoked;

    // flag on if the undo() operation was invoked on this instance (it does not have to change state)
    private boolean undoInvoked;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext executionContext) throws Exception {

        executeInvoked = true;

        if (executionBroken) {
            throw new MockSequenceExecutionException();
        }

        return true;
    }

    @Override
    public boolean undo(SequenceExecutionContext executionContext) {

        undoInvoked = true;

        if (undoBroken) {
            throw new RuntimeException("SYNTHETIC UNCHECKED");
        }

        if (executeInvoked) {

            //
            // TODO we should make this configurable
            //

            log.info(this + " was previously executed, undoing");

            return true;
        }
        else {

            //
            // we were not executed, there's nothing to undo
            //

            return false;
        }
    }

    @Override
    public boolean didExecuteChangeState() {

        throw new NotYetImplementedException("didExecuteChangeState()");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @param b if true, then the execution will fail by throwing a MockSequenceExecutionException.
     */
    public void setExecutionBroken(boolean b) {

        this.executionBroken = b;
    }

    public boolean wasExecuteInvoked() {
        return executeInvoked;
    }

    /**
     * @param b if true, then the undo will fail by throwing an unchecked exception.
     */
    public void setUndoBroken(boolean b) {

        this.undoBroken = b;
    }

    /**
     * @return true if the undo() operation was invoked on this instance (it does not have to change state)
     */
    public boolean wasUndoInvoked() {
        return undoInvoked;
    }

    @Override
    public String toString() {

        return "mock sequence";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
