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

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.command.CommandBase;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.clad.option.BooleanOption;
import io.novaordis.clad.option.Option;
import io.novaordis.release.clad.ReleaseApplicationRuntime;
import io.novaordis.release.model.Project;
import io.novaordis.release.model.ProjectBuilder;
import io.novaordis.release.model.maven.MavenProjectBuilder;
import io.novaordis.release.sequences.BuildSequence;
import io.novaordis.release.sequences.CompletionSequence;
import io.novaordis.release.sequences.InstallSequence;
import io.novaordis.release.sequences.PublishSequence;
import io.novaordis.release.sequences.QualificationSequence;
import io.novaordis.release.sequences.Sequence;
import io.novaordis.release.sequences.SequenceController;
import io.novaordis.release.sequences.SequenceExecutionContext;
import io.novaordis.release.version.Version;
import io.novaordis.release.version.VersionFormatException;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * See
 * {@linktourl https://kb.novaordis.com/index.php/Nova_Ordis_Release_Tools_User_Manual_-_Concepts#The_Release_Sequence}
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/8/16
 */
public class ReleaseCommand extends CommandBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ReleaseCommand.class);

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @return the types of sequences that will be executed as part of the command, in the order in which will be
     * executed.
     */
    public static List<Class<? extends Sequence>> getSequenceTypes() {

        return Arrays.asList(
                QualificationSequence.class,
                BuildSequence.class,
                PublishSequence.class,
                InstallSequence.class,
                CompletionSequence.class
        );
    }

    // Package protected static ----------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ProjectBuilder projectBuilder;
    private String releaseLabel;
    private ReleaseMode mode;
    private boolean noPush;

    // Constructors ----------------------------------------------------------------------------------------------------

    public ReleaseCommand() {

        //
        // we're Maven based by default
        //

        this.projectBuilder = new MavenProjectBuilder();
    }

    // CommandBase overrides -------------------------------------------------------------------------------------------

    @Override
    public Set<Option> optionalOptions() {

        Set<Option> result = new HashSet<>();
        result.add(new BooleanOption("no-push"));
        return result;
    }

    @Override
    public void configure(int from, List<String> commandLineArguments) throws Exception {

        log.debug("configuring " + this + " from " + commandLineArguments);

        super.configure(from, commandLineArguments);

        //noinspection Convert2streamapi
        for(Option o: getOptions()) {
            if (new BooleanOption("no-push").equals(o)) {
                noPush = ((BooleanOption)o).getValue();
            }
        }

        //
        // identifying release mode
        //

        outer: for(int i = from; i < commandLineArguments.size(); i++) {

            String crt = commandLineArguments.get(i);

            if (mode != null) {
                break;
            }

            //
            // identify the release mode
            //

            for(ReleaseMode mode: ReleaseMode.values()) {

                //
                // ignore "custom"
                //
                if (!ReleaseMode.custom.equals(mode)) {

                    if (mode.toString().equals(crt)) {

                        setMode(mode);
                        commandLineArguments.remove(i);
                        break outer;
                    }
                }
            }

            //
            // "custom" release string
            //

            setMode(ReleaseMode.custom);
            commandLineArguments.remove(i);
            this.releaseLabel = crt;
        }
    }

    @Override
    public void execute(Configuration c, ApplicationRuntime runtime) throws Exception {

        ReleaseApplicationRuntime r = (ReleaseApplicationRuntime)runtime;
        Project p = projectBuilder.build(r.getCurrentDirectory());

        if (ReleaseMode.info.equals(mode)) {

            info(r, p);
        }
        else if (mode.isIncrement()) {

            incrementRelease(c, r, p, mode);
        }
        else {
            throw new RuntimeException("NOT YET IMPLEMENTED: release " + mode);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public ReleaseMode getMode() {

        return mode;
    }

    /**
     * The value explicitly specified as a "custom" release label.
     *
     * May return null.
     */
    public String getReleaseLabel() {

        return releaseLabel;
    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setMode(ReleaseMode mode) {

        this.mode = mode;
    }

    void setProjectBuilder(ProjectBuilder b) {

        this.projectBuilder = b;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void info(ReleaseApplicationRuntime r, Project m) throws UserErrorException {

        try {

            Version version = m.getVersion();
            r.info(version.getLiteral());
        }
        catch(VersionFormatException e) {

            throw new UserErrorException(e.getMessage());
        }

    }

    private void incrementRelease(Configuration c, ReleaseApplicationRuntime r, Project m, ReleaseMode rm)
            throws Exception {

        //
        // Install the "known" sequences required by a release into a new controller instance created for this run
        //
        SequenceController controller = new SequenceController(rm, noPush, getSequenceTypes());

        boolean successfulRelease = false;

        SequenceExecutionContext ctx = null;

        try {

            ctx = controller.execute(c, r, m);
            successfulRelease = true;
        }
        finally {

            if (!successfulRelease) {

                ctx = controller.undo(c, r, m);
            }

            r.setLastExecutionContext(ctx);
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
