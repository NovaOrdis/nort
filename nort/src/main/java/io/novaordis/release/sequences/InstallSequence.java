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

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.OutputUtil;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.model.Artifact;
import io.novaordis.release.model.Project;
import io.novaordis.release.model.ArtifactType;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class InstallSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(InstallSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executeChangedState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public InstallSequence() {

        this.executeChangedState = false;
    }

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext c) throws Exception {

        log.debug("executing the install sequence ...");

        ApplicationRuntime runtime = c.getRuntime();

        //
        // if we are a library, the publish sequence already published the artifacts in the repository, info and
        // skip
        //

        Project p = c.getProject();

        List<Artifact> artifacts = p.getArtifacts();

        for(Iterator<Artifact> i = artifacts.iterator(); i.hasNext(); ) {

            Artifact a = i.next();

            if (ArtifactType.JAR_LIBRARY.equals(a.getType())) {

                File f = a.getRepositoryFile();
                runtime.info(f.getName() + " " + a.getType().getLabel() +
                        " already published to the Maven artifact repository");

                i.remove();
            }
        }

        if (artifacts.isEmpty()) {

            return false;
        }

        //
        // for the time being we only support installation of ONE binary distributions
        //

        if (artifacts.size() > 1) {
            throw new RuntimeException("NOT YET IMPLEMENTED: for the time being we only can install one binary distribution");
        }

        ArtifactType t = artifacts.get(0).getType();

        if (!ArtifactType.BINARY_DISTRIBUTION.equals(t)) {
            throw new RuntimeException("NOT YET IMPLEMENTED: don't know how to install " + t + " artifacts");
        }

        //
        // lookup the local artifact repository root, fail if not available
        //

        Configuration conf = c.getConfiguration();
        String s = conf.get(ConfigurationLabels.LOCAL_ARTIFACT_REPOSITORY_ROOT);

        if (s == null) {

            throw new UserErrorException("the local artifact repository root not configured");
        }

        File artifactRepositoryRoot = new File(s);

        if (!artifactRepositoryRoot.isDirectory()) {

            throw new UserErrorException("the local artifact repository root " + artifactRepositoryRoot.getAbsolutePath() + " is not a valid directory");
        }

        //
        // figure out the distribution file, in the artifact repository
        //

        List<Artifact> binaryDistributions = p.getArtifacts(ArtifactType.BINARY_DISTRIBUTION);

        if (binaryDistributions.isEmpty()) {

            throw new IllegalStateException("this project has no binary distributions");
        }

        if (binaryDistributions.size() > 1) {
            throw new RuntimeException("NOT YET IMPLEMENTED: we don't know how to handle the case when the project has more than one binary distribution");
        }

        Artifact binaryDistribution = binaryDistributions.get(0);
        File f = binaryDistribution.getRepositoryFile();

        //
        // resolve the file relative to the local artifact repository root
        //

        f = new File(artifactRepositoryRoot, f.getPath());

        if (!f.isFile()) {

            throw new UserErrorException("binary distribution artifact " + f + " not found in the local artifact repository - was the artifact published?");
        }

        //
        // lookup the runtime directory, fail if not available
        //

        s = conf.get(ConfigurationLabels.RUNTIME_DIRECTORY);

        if (s == null) {

            throw new UserErrorException("the runtime directory not configured");
        }

        File rd = new File(s);

        if (!rd.isDirectory()) {

            throw new UserErrorException("the runtime directory " + rd + " is not a valid directory");
        }

        if (!rd.canWrite()) {

            throw new UserErrorException("the runtime directory " + rd + " is not writable");
        }

        //
        // unzip the distribution file in the runtime directory
        //

        ApplicationRuntime rt = c.getRuntime();
        NativeExecutionResult r = OutputUtil.handleNativeCommandOutput(
                OS.getInstance().execute(rd, "unzip " + f.getAbsolutePath()), rt, conf);

        if (r.isFailure()) {
            throw new UserErrorException("failed to extract " + f.getAbsolutePath() + " in " + rd.getAbsolutePath());
        }

        //
        // execute the "install" script embedded with the release - we assume the directory created by extracting the
        // binary distribution has the same name as the binary distribution zip, without the extension
        //

        String name = Files.basename(f, ".zip");

        File installationScript = new File(rd, name + "/bin/.install");

        if (!installationScript.isFile() || !installationScript.canExecute()) {

            throw new UserErrorException(
                    "no installation script " + installationScript.getAbsolutePath() + " found or the file is not executable");
        }

        //
        // execute the installation script
        //

        File installationScriptDirectory = installationScript.getParentFile();

        NativeExecutionResult r2 = OutputUtil.handleNativeCommandOutput(
                OS.getInstance().execute(installationScriptDirectory, installationScript.getName()), rt, conf);

        if (r2.isFailure()) {
            throw new UserErrorException("installation failed");
        }

        log.info("installation ok");
        this.executeChangedState = true;
        return true;
    }

    @Override
    public boolean undo(SequenceExecutionContext context) {

        if (!executeChangedState) {

            // noop
            return false;
        }

        throw new RuntimeException("undo() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
