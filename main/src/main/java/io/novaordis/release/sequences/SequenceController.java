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
import io.novaordis.release.clad.ReleaseApplicationRuntime;
import io.novaordis.release.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An instance that executes sequences in a pre-defined order.
 *
 * The sequence instances do not know whether they have follow up sequences, the controller does.
 *
 * The controller maintains a history of the method invocations that occurred when the context was valid. It is the
 * responsibility of the controller to record the history, but the history is accessible via the context, and
 * publicly;
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class SequenceController {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(SequenceController.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private List<Sequence> sequences;

    private ReleaseMode rm;
    private boolean noPush;

    private ExecutionHistory history;

    // Constructors ----------------------------------------------------------------------------------------------------

    public SequenceController(ReleaseMode rm, boolean noPush, List<Class<? extends Sequence>> sequenceTypes)
            throws Exception{

        this(rm, noPush);

        for(Class<? extends Sequence> type: sequenceTypes) {

            Sequence s = type.newInstance();
            add(s);
        }
    }

    public SequenceController(ReleaseMode rm, boolean noPush, Sequence ... sequences) {

        this.rm = rm;
        this.noPush = noPush;
        this.sequences = new ArrayList<>();
        this.history = new ExecutionHistory();

        if (sequences != null) {

            for(Sequence s: sequences) {
                add(s);
            }
        }
    }

    protected SequenceController() {
        this(null, true);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Execute all sequences in the order in which they were added, within a new context created specifically for this
     * operation.
     *
     * If a sequence execution fails, the controller stops immediately and forwards the exception to the calling layer.
     *
     * @return the SequenceExecutionContext for this operation.
     */
    public SequenceExecutionContext execute(ReleaseApplicationRuntime r, Project m) throws Exception {

        SequenceExecutionContext context = new SequenceExecutionContext(r, m, rm, noPush, history);

        for(Sequence s: sequences) {

            boolean success = false;
            boolean stateChanged = false;

            try {

                stateChanged = s.execute(context);
                success = true;
            }
            finally {

                updateHistory("execute", s, success, stateChanged);
            }
        }

        return context;
    }

    /**
     * Revert all changes to the work area and related resources that were introduced by the last execution, for
     * all component sequences, within a context created specifically for this operation.
     *
     * @return true if the undo operation modified state, false otherwise.
     */
    public SequenceExecutionContext undo(ReleaseApplicationRuntime r, Project m) {

        SequenceExecutionContext context = new SequenceExecutionContext(r, m, rm, noPush, history);

        //
        // undo in the reverse order
        //

        for(int i = sequences.size() - 1; i >=0; i--) {

            //
            // if an undo fails, log but don't prevent other sequences from being undone
            //

            Sequence s = sequences.get(i);

            boolean success = false;
            boolean stateChanged = false;

            try {

                stateChanged = s.undo(context);
                success = true;
            }
            catch(Throwable t) {

                String genericName = s.getClass().getSimpleName();
                genericName = genericName.replaceAll("Sequence", "").toLowerCase();
                String msg = t.getMessage();
                log.warn("failed to undo " + genericName + " sequence" + (msg == null ? "" : ": " + msg));
                log.debug("undo failure", t);
            }
            finally {

                updateHistory("undo", s, success, stateChanged);
            }
        }

        return context;
    }

    /**
     * @return the number of sequences the controller knows about.
     */
    public int getCount() {

        return sequences.size();
    }

    public ExecutionHistory getHistory() {
        return history;
    }

    @Override
    public String toString() {

        if (sequences == null) {
            return "null sequence list";
        }

        String s = "sequences: ";

        for(Iterator<Sequence> i = sequences.iterator(); i.hasNext(); ) {

            s += i.next().getClass().getSimpleName();
            s = s.replace("Sequence", "");
            s = s.toLowerCase();

            if (i.hasNext()) {
                s += ", ";
            }
        }

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    /**
     * Adds a sequence to the list of sequences to be executed. The sequences will be executed in the order in which
     * they were added.
     */
    void add(Sequence sequence) {

        sequences.add(sequence);
    }

    /**
     * @param operationName "update" or "undo"
     * @param success true if the method invocation on sequence was a success (did not throw exception), false otherwise
     * @param stateChanged true if external state changed (files were written or updated on disk, artifacts were
     *                     pushed to repositories, etc) as result of the method invocation on sequence, false otherwise
     *
     */
    void updateHistory(String operationName, Sequence s, boolean success, boolean stateChanged) {

        history.record(operationName, s, success, stateChanged);
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}

