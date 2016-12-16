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

import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.Option;
import io.novaordis.release.clad.ReleaseApplicationRuntime;
import io.novaordis.release.sequences.SequenceExecutionContext;
import io.novaordis.utilities.NotYetImplementedException;

import java.io.File;
import java.io.OutputStream;
import java.util.Set;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class MockReleaseApplicationRuntime extends ReleaseApplicationRuntime {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File currentDirectory;

    private String warningContent;

    private String binaryDistributionTopLevelDirectoryName;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockReleaseApplicationRuntime() {

        this.warningContent = "";
        this.binaryDistributionTopLevelDirectoryName = "mock-top-level-directory";
    }

    // ReleaseApplicationRuntime overrides -----------------------------------------------------------------------------

    @Override
    public String getName() {
        throw new RuntimeException("getName() NOT YET IMPLEMENTED");
    }

    @Override
    public String getDefaultCommandName() {
        throw new RuntimeException("getDefaultCommandName() NOT YET IMPLEMENTED");
    }

    @Override
    public String getHelpFilePath() {
        throw new RuntimeException("getHelpFilePath() NOT YET IMPLEMENTED");
    }

    @Override
    public Set<Option> requiredGlobalOptions() {
        throw new RuntimeException("requiredGlobalOptions() NOT YET IMPLEMENTED");
    }

    @Override
    public Set<Option> optionalGlobalOptions() {
        throw new RuntimeException("optionalGlobalOptions() NOT YET IMPLEMENTED");
    }

    @Override
    public void init(Configuration configuration) throws Exception {
        super.init(configuration);
    }

    @Override
    public void setStdoutOutputStream(OutputStream outputStream) {
        super.setStdoutOutputStream(outputStream);
    }

    @Override
    public OutputStream getStdoutOutputStream() {
        throw new RuntimeException("getStdoutOutputStream() NOT YET IMPLEMENTED");
    }

    @Override
    public void setStderrOutputStream(OutputStream outputStream) {
        super.setStderrOutputStream(outputStream);    }

    @Override
    public OutputStream getStderrOutputStream() {
        throw new RuntimeException("getStderrOutputStream() NOT YET IMPLEMENTED");
    }

    @Override
    public void info(String s) {
        System.out.println(s);
    }

    @Override
    public void warn(String s) {

        warningContent += s + "\n";
    }

    @Override
    public void error(String s) {

        throw new NotYetImplementedException("error()");
    }

    @Override
    public File getCurrentDirectory() {

        return currentDirectory;
    }

    @Override
    public SequenceExecutionContext getLastExecutionContext() {

        return super.getLastExecutionContext();
    }

    @Override
    public void setLastExecutionContext(SequenceExecutionContext c) {

        super.setLastExecutionContext(c);
    }

    @Override
    public ZipHandler getZipHandler() {

        return file -> binaryDistributionTopLevelDirectoryName;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public void setCurrentDirectory(File d) {
        this.currentDirectory = d;
    }

    public String getWarningContent() {
        return warningContent;
    }

    public void setBinaryDistributionTopLevelDirectoryName(String s) {
        this.binaryDistributionTopLevelDirectoryName = s;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
