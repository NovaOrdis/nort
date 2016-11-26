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

import io.novaordis.utilities.os.NativeExecutionException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import io.novaordis.utilities.os.OSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/16/16
 */
public class MockOS implements OS {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(MockOS.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean allCommandsSucceedByDefault;
    private boolean allCommandsFail;
    private List<CommandAndOutput> commandsThatFail;
    private List<CommandAndOutput> commandsThatSucceed;

    private List<String> executedCommands;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockOS() {

        this.commandsThatFail = new ArrayList<>();
        this.commandsThatSucceed = new ArrayList<>();
        this.executedCommands = new ArrayList<>();
    }

    // OS implementation -----------------------------------------------------------------------------------------------

    @Override
    public OSConfiguration getConfiguration() {
        throw new RuntimeException("getConfiguration() NOT YET IMPLEMENTED");
    }

    @Override
    public NativeExecutionResult execute(String command) throws NativeExecutionException {

        return execute(null, command);
    }

    @Override
    public NativeExecutionResult execute(File directory, String command) throws NativeExecutionException {

        //
        // use the same t
        //

        //
        // use the same type of logging as the actual implementations
        //

        OS.logExecution(log, directory, command);

        executedCommands.add(command);

        if (allCommandsFail) {
            return new NativeExecutionResult(1, "mock \"" + command + "\" stdout", "mock \"" + command + "\" stderr");
        }

        for(CommandAndOutput c: commandsThatFail) {

            if (c.command.equals(command)) {

                return new NativeExecutionResult(1, c.stdout, c.stderr);
            }
        }

        if (allCommandsSucceedByDefault) {

            return new NativeExecutionResult(0, "mock \"" + command + "\" stdout", "mock \"" + command + "\" stderr");
        }

        for(CommandAndOutput c: commandsThatSucceed) {

            if (c.command.equals(command)) {

                return new NativeExecutionResult(0, c.stdout, c.stderr);
            }
        }

        throw new RuntimeException("WE DON'T KNOW HOW TO HANDLE " + command);
    }

    @Override
    public String getName() {
        throw new RuntimeException("getName() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * @see MockOS#allCommandsSucceedByDefault()
     */
    public void addToCommandsThatFail(String command) {

        addToCommandsThatFail(command, "mock \"" + command + "\" stdout", "mock \"" + command + "\" stderr");
    }

    public void addToCommandsThatFail(String command, String stdout, String stderr) {

        commandsThatFail.add(new CommandAndOutput(command, stdout, stderr));
    }

    public void addToCommandsThatSucceed(String command) {

        addToCommandsThatSucceed(command, "mock \"" + command + "\" stdout", "mock \"" + command + "\" stderr");
    }

    public void addToCommandsThatSucceed(String command, String stdout, String stderr) {

        commandsThatSucceed.add(new CommandAndOutput(command, stdout, stderr));
    }

    /**
     * Configure the OS to "successfully" execute all commands sent into it, with the exception those that were
     * specified as "commands that fail", if any.
     *
     * @see MockOS#addToCommandsThatFail(String)
     */
    public void allCommandsSucceedByDefault() {

        this.allCommandsSucceedByDefault = true;
    }

    public void allCommandsFail() {

        this.allCommandsFail = true;
    }


    public void reset() {

        commandsThatFail.clear();
        commandsThatSucceed.clear();
        executedCommands.clear();
        allCommandsFail = false;
    }

    /**
     * @return the list of commands that were executed (successfully or unsuccessfully) in the order in which they were
     * executed.
     */
    public List<String> getHistory() {

        return executedCommands;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

    private class CommandAndOutput {

        public String command;
        public String stdout;
        public String stderr;

        public CommandAndOutput(String command) {

            if (command == null) {
                throw new IllegalArgumentException("null command");
            }
            this.command = command;
            this.stdout = "";
            this.stderr = "";
        }

        public CommandAndOutput(String command, String stdout, String stderr) {

            this(command);

            if (stdout == null) {
                throw new IllegalArgumentException("stdout command");
            }

            if (stderr == null) {
                throw new IllegalArgumentException("stderr command");
            }

            this.stdout = stdout;
            this.stderr = stderr;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (this.command == null) {
                return false;
            }

            if (!(o instanceof CommandAndOutput)) {
                return false;
            }

            CommandAndOutput that = (CommandAndOutput)o;

            return this.command.equals(that.command);
        }

        @Override
        public int hashCode() {

            if (command == null) {
                return 0;
            }

            return 7 + 17 * command.hashCode();
        }
    }
}
