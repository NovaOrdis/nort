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
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/18/16
 */
public class SequenceOperation {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private String methodName;
    private Sequence target;
    private boolean wasSuccess;
    private boolean didChangeState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public SequenceOperation(String methodName, Sequence target, boolean wasSuccess, boolean didChangeState) {

        this.methodName = methodName;
        this.target = target;
        this.wasSuccess = wasSuccess;
        this.didChangeState = didChangeState;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getMethodName() {
        return methodName;
    }

    public Sequence getTarget() {
        return target;
    }

    public boolean wasSuccess() {
        return wasSuccess;
    }

    public boolean didChangeState() {
        return didChangeState;
    }

    @Override
    public String toString() {

        String s;

        if (target == null) {

            s = "null";
        }
        else {
            s = target.getClass().getSimpleName();
        }

        s += ".";

        if (methodName == null) {

            s += "null ";
        }
        else {
            s += methodName + "() ";
        }

        s += "success=" + wasSuccess() + ", ";
        s += "changed state=" + didChangeState();

        return s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
