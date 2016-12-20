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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class SequenceControllerTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(SequenceControllerTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // add -------------------------------------------------------------------------------------------------------------

    @Test
    public void add() throws Exception {

        SequenceController c = new SequenceController();

        MockSequence s1 = new MockSequence();

        c.add(s1);

        assertEquals(1, c.getCount());

        MockSequence s2 = new MockSequence();

        c.add(s2);

        assertEquals(2, c.getCount());
    }

    // execute ---------------------------------------------------------------------------------------------------------

    @Test
    public void execute_FirstSequenceFails() throws Exception {

        SequenceController c = new SequenceController();

        MockSequence s1 = new MockSequence();
        s1.setExecutionBroken(true);

        MockSequence s2 = new MockSequence();

        c.add(s1);
        c.add(s2);

        try {
            c.execute(null, null);
            fail("should have thrown exception");
        }
        catch(MockSequenceExecutionException e) {
            log.info(e.getMessage());
        }

        assertTrue(s1.wasExecuteInvoked());
        assertFalse(s2.wasExecuteInvoked());
    }

    @Test
    public void execute_MiddleSequenceFails() throws Exception {

        SequenceController c = new SequenceController();

        MockSequence s1 = new MockSequence();

        MockSequence s2 = new MockSequence();
        s2.setExecutionBroken(true);

        MockSequence s3 = new MockSequence();

        c.add(s1);
        c.add(s2);
        c.add(s3);

        try {
            c.execute(null, null);
            fail("should have thrown exception");
        }
        catch(MockSequenceExecutionException e) {
            log.info(e.getMessage());
        }

        assertTrue(s1.wasExecuteInvoked());
        assertTrue(s2.wasExecuteInvoked());
        assertFalse(s3.wasExecuteInvoked());
    }

    @Test
    public void execute_LastSequenceFails() throws Exception {

        SequenceController c = new SequenceController();

        MockSequence s1 = new MockSequence();

        MockSequence s2 = new MockSequence();

        MockSequence s3 = new MockSequence();
        s3.setExecutionBroken(true);

        c.add(s1);
        c.add(s2);
        c.add(s3);

        try {
            c.execute(null, null);
            fail("should have thrown exception");
        }
        catch(MockSequenceExecutionException e) {
            log.info(e.getMessage());
        }

        assertTrue(s1.wasExecuteInvoked());
        assertTrue(s2.wasExecuteInvoked());
        assertTrue(s3.wasExecuteInvoked());
    }

    @Test
    public void execute() throws Exception {

        //
        // make sure the sequences were executed in order
        //

        SequenceController c = new SequenceController();

        MockSequence s1 = new MockSequence();

        MockSequence s2 = new MockSequence();

        MockSequence s3 = new MockSequence();

        c.add(s1);
        c.add(s2);
        c.add(s3);

        SequenceExecutionContext ctx = c.execute(null, null);

        assertTrue(s1.wasExecuteInvoked());
        assertTrue(s2.wasExecuteInvoked());
        assertTrue(s3.wasExecuteInvoked());

        //
        // make sure the sequences were executed in order
        //

        ExecutionHistory h = ctx.getHistory();

        assertEquals(3, h.length());

        assertEquals(s1, h.getOperation(0).getTarget());
        assertEquals(s2, h.getOperation(1).getTarget());
        assertEquals(s3, h.getOperation(2).getTarget());

        assertEquals("execute", h.getOperation(0).getMethodName());
        assertEquals("execute", h.getOperation(1).getMethodName());
        assertEquals("execute", h.getOperation(2).getMethodName());

    }

    // undo ------------------------------------------------------------------------------------------------------------

    @Test
    public void undo() throws Exception {

        MockSequence s1 = new MockSequence();
        MockSequence s2 = new MockSequence();
        MockSequence s3 = new MockSequence();

        SequenceController c = new SequenceController(ReleaseMode.snapshot, true, s1, s2, s3);

        SequenceExecutionContext ctx = c.undo(null, null);

        ExecutionHistory h = ctx.getHistory();

        //
        // make sure an undo() is propagated to all sequences, in the inverse order
        //

        assertEquals(3, h.length());

        assertEquals(s3, h.getOperation(0).getTarget());
        assertEquals(s2, h.getOperation(1).getTarget());
        assertEquals(s1, h.getOperation(2).getTarget());

        assertEquals("undo", h.getOperation(0).getMethodName());
        assertEquals("undo", h.getOperation(1).getMethodName());
        assertEquals("undo", h.getOperation(2).getMethodName());

        assertTrue(s1.wasUndoInvoked());
        assertTrue(s2.wasUndoInvoked());
        assertTrue(s3.wasUndoInvoked());
    }

    @Test
    public void undo_OneOfTheSequencesFailsUnchecked() throws Exception {

        //
        // make sure that even if a sequence undo fails unchecked, the others sequences (especially the subsequent
        // ones) still undo
        //

        MockSequence s1 = new MockSequence();
        MockSequence s2 = new MockSequence();
        MockSequence s3 = new MockSequence();
        s3.setUndoBroken(true); // we break the last one because we start the undo process with this

        SequenceController c = new SequenceController(ReleaseMode.snapshot, true, s1, s2, s3);

        SequenceExecutionContext ctx = c.undo(null, null);

        ExecutionHistory h = ctx.getHistory();

        //
        // make sure an undo() is propagated to all sequences, in the inverse order
        //

        assertEquals(3, h.length());

        assertEquals(s3, h.getOperation(0).getTarget());
        assertEquals(s2, h.getOperation(1).getTarget());
        assertEquals(s1, h.getOperation(2).getTarget());

        assertEquals("undo", h.getOperation(0).getMethodName());
        assertEquals("undo", h.getOperation(1).getMethodName());
        assertEquals("undo", h.getOperation(2).getMethodName());

        assertTrue(s1.wasUndoInvoked());
        assertTrue(s2.wasUndoInvoked());
        assertTrue(s3.wasUndoInvoked());
    }

    // history ---------------------------------------------------------------------------------------------------------

    @Test
    public void updateHistory() throws Exception {

        SequenceController c = new SequenceController();

        MockSequence ms = new MockSequence();
        c.updateHistory("update", ms, true, true);

        ExecutionHistory h = c.getHistory();
        assertEquals(1, h.length());

        SequenceOperation so = h.getOperation(0);
        assertEquals("update", so.getMethodName());
        assertEquals(ms, so.getTarget());
        assertTrue(so.wasSuccess());
        assertTrue(so.didChangeState());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
