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

/**
 * A series of steps that trigger (usually complex) interactions between the work area, repositories, build tools, etc.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public interface Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * If the execution results in state changes, undo() will revert to the state existent before the execution.
     *
     * By "changing the state"  we understand modification of project metadata (such as incrementing the version
     * information in the project descriptor or pom.xml), modification of the code base as result of the release
     * sequence, installation of various artifacts in various repositories. Some actions, even if they are executed and
     * modify state on disk, do not count as "changing state". For example, the fact that we ran tests or we modified
     * files in the target directory does not count as state change.
     *
     * @{linkUrl https://kb.novaordis.com/index.php/Nova_Ordis_Release_Tools_User_Manual_-_Concepts#State_Change}
     *
     * @return true if execution changed state, false otherwise
     */
    boolean execute(SequenceExecutionContext executionContext) throws Exception;

    /**
     * Undo the changes introduced by the <b>last</b> execution. Even if the method does not throw a checked exception
     * (which means an undo must succeed, and if it fails, log error at best), the implementation can still throw
     * unchecked exceptions, so the invoking layer must be prepared to deal with that and if the undo is part of a
     * sequence, to make sure the subsequent undos are still performed.
     *
     * If the  <b>last</b> execution did not introduce any changes, the invocation must be a noop.
     *
     * @return true if undo() changed state, false if it was a noop.
     */
    boolean undo(SequenceExecutionContext executionContext);

    /**
     * True if the execute() changed state.
     */
    boolean didExecuteChangeState();

}
