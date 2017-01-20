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

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.ReleaseMode;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.clad.ReleaseApplicationRuntime;
import io.novaordis.release.model.Project;
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;

import java.util.HashMap;
import java.util.Map;

/**
 * A communication mechanism between sequences being executed together.
 *
 * It exposes typed state and a generic untyped map that can be used by sequence as they wish. It also offers typed
 * access for a small number of essential state elements (whether tests were executed or not, currentVersion, etc).
 * Internally, we're using the generic map for storage, but the API adds the convenience of typing.
 *
 * Upon construction, the initial state is correctly initialized with current version extracted from Project, etc.
 *
 * Contexts are created for SequenceController.execute() or SequenceController.undo() operations.
 *
 * @see SequenceController#execute(ReleaseApplicationRuntime, Project)
 * @see SequenceController#undo(ReleaseApplicationRuntime, Project)
 *
 * @see ExecutionHistory
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/18/16
 */
public class SequenceExecutionContext {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String TESTS_WERE_EXECUTED_KEY = "TESTS_WERE_EXECUTED";
    public static final String NO_PUSH_KEY = "NO_PUSH";
    public static final String RELEASE_MODE_KEY = "RELEASE_MODE";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private Configuration configuration;
    private ReleaseApplicationRuntime runtime;
    private Project project;
    private Map<Object, Object> state;

    /**
     * @see SequenceExecutionContext#getHistory()
     */
    private ExecutionHistory history;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param h the controller's operation history. The context has access to it for reading, but it won't maintain
     *          it, it is the controller who maintains the history.
     *
     * @throws IllegalStateException on invalid project state
     */
    public SequenceExecutionContext(
            ReleaseApplicationRuntime r, Project m, ReleaseMode rm, boolean noPush, ExecutionHistory h) {

        this.state = new HashMap<>();

        if (r == null) {
            throw new IllegalArgumentException("null runtime");
        }

        this.configuration = r.getConfiguration();
        this.runtime = r;
        this.project = m;

        if (project != null) {

            //
            // initialize current version
            //
            try {

                setCurrentVersion(project.getVersion());
            }
            catch(VersionFormatException e) {

                throw new IllegalStateException(e);
            }
        }
        this.history = h;

        if (rm == null) {

            //
            // this is the default
            //
            rm = ReleaseMode.snapshot;
        }

        setReleaseMode(rm);
        setNoPush(noPush);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Configuration getConfiguration() {
        return configuration;
    }

    public ReleaseApplicationRuntime getRuntime() {
        return runtime;
    }

    public Project getProject() {
        return project;
    }

    /**
     * Default is ReleaseMode.snapshot.
     */
    public ReleaseMode getReleaseMode() {

        return (ReleaseMode) state.get(RELEASE_MODE_KEY);
    }

    /**
     * @return true if the tests were executed by at least one of the sequences that ran so far. The tests do not
     * need to pass in order for this flag to be set to true.
     */
    public boolean wereTestsExecuted() {

        Boolean b = (Boolean) state.get(TESTS_WERE_EXECUTED_KEY);

        if (b == null) {
            return false;
        }

        return b;
    }

    /**
     * May return null.
     */
    public Version getCurrentVersion() {

        //
        // because the current version must be exposed as a runtime variable, use the underlying variable provider
        // for storage
        //
        String s = runtime.getVariableValue(ConfigurationLabels.CURRENT_VERSION);

        if (s == null) {

            return null;
        }

        try {

            return new Version(s);
        }
        catch(Exception e) {

            throw new IllegalStateException(e);
        }
    }

    public boolean isNoPush() {

        Boolean b = (Boolean) state.get(NO_PUSH_KEY);

        if (b == null) {
            return false;
        }

        return b;
    }

    // generic state access --------------------------------------------------------------------------------------------

    /**
     * Allows clients to set generic untyped state within the context.
     */
    public void set(Object key, Object value) {

        state.put(key, value);
    }

    /**
     * Allows clients to retrieve generic untyped state within the context.
     *
     * @return null if no such key exists.
     */
    public Object get(Object key) {

        return state.get(key);
    }

    /**
     * @return the controller's operation history. The context has access to it for reading, but it won't maintain it,
     * it is the controller who maintains the history.
     */
    public ExecutionHistory getHistory() {
        return history;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setTestsExecuted(boolean b) {

        state.put(TESTS_WERE_EXECUTED_KEY, b);
    }

    void setCurrentVersion(Version v) {

        //
        // because the current version must be exposed as a runtime variable, use the underlying variable provider
        // for storage
        //

        String literal = null;

        if (v != null) {
            literal = v.getLiteral();
        }

        runtime.setVariableValue(ConfigurationLabels.CURRENT_VERSION, literal);
    }

    void setNoPush(boolean b) {

        state.put(NO_PUSH_KEY, b);
    }

    void setReleaseMode(ReleaseMode rm) {

        state.put(RELEASE_MODE_KEY, rm);
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
