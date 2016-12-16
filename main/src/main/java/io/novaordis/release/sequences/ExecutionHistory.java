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

import java.util.ArrayList;
import java.util.List;

/**
 * A class that maintains the history of method invocations on sequence instances for a specific controller instance.
 *
 * @see SequenceExecutionContext
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/18/16
 */
public class ExecutionHistory {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<SequenceOperation> operations;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ExecutionHistory() {

        this.operations = new ArrayList<>();
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @param operationName "update" or "undo"
     * @param success true if the method invocation on sequence was a success (did not throw exception), false otherwise
     * @param stateChanged true if external state changed (files were written or updated on disk, artifacts were
     *                     pushed to repositories, etc) as result of the method invocation on sequence, false otherwise
     */
    public void record(String operationName, Sequence s, boolean success, boolean stateChanged) {

        operations.add(new SequenceOperation(operationName, s, success, stateChanged));
    }

    public int length() {
        return operations.size();
    }

    /**
     * THe method behaves similarly to List.get() on out-of-bounds-access.
     *
     * @exception IndexOutOfBoundsException
     */
    public SequenceOperation getOperation(int index) {

        return operations.get(index);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
