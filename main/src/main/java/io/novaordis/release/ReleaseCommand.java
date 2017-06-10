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
import io.novaordis.clad.option.BooleanOption;
import io.novaordis.clad.option.Option;
import io.novaordis.release.clad.ConfigurationLabels;
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
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    public static final String NO_TESTS_OPTION_LITERAL = "no-tests";
    public static final String NO_PUSH_OPTION_LITERAL = "no-push";
    public static final String NO_INSTALL_OPTION_LITERAL = "no-install";

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @return the default types of sequences that will be executed as part of the command, in the order in which will
     * be executed.
     */
    public static List<Class<? extends Sequence>> getSequenceTypes() {

        return getSequenceTypes(false);
    }

    /**
     * @param noInstall if true, the InstallSequence won't be part of the result.
     *
     * @return the types of sequences that will be executed as part of the command, in the order in which will be
     * executed.
     */
    public static List<Class<? extends Sequence>> getSequenceTypes(boolean noInstall) {

        List<Class<? extends Sequence>> result = new ArrayList<>();

        result.add(QualificationSequence.class);
        result.add(BuildSequence.class);
        result.add(PublishSequence.class);

        if (!noInstall) {
            result.add(InstallSequence.class);
        }

        result.add(CompletionSequence.class);

        return result;
    }

    // Package protected static ----------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ProjectBuilder projectBuilder;
    private ReleaseMode mode;

    private boolean noTests;
    private boolean noPush;
    private boolean noInstall;

    private SequenceController controller;

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
        result.add(new BooleanOption(NO_TESTS_OPTION_LITERAL));
        result.add(new BooleanOption(NO_PUSH_OPTION_LITERAL));
        result.add(new BooleanOption(NO_INSTALL_OPTION_LITERAL));
        return result;
    }

    /**
     * The command requires at least one argument, which is the release mode and possibly a custom release label.
     */
    @Override
    public void configure(int from, List<String> commandLineArguments) throws Exception {

        log.debug("configuring " + this + " from " + commandLineArguments);

        super.configure(from, commandLineArguments);

        BooleanOption o = (BooleanOption)getOption(new BooleanOption(NO_TESTS_OPTION_LITERAL));
        noTests = o != null && o.getValue();

        log.debug("no tests: " + noTests);

        o = (BooleanOption)getOption(new BooleanOption(NO_PUSH_OPTION_LITERAL));
        noPush = o != null && o.getValue();

        log.debug("no push: " + noPush);

        o = (BooleanOption)getOption(new BooleanOption(NO_INSTALL_OPTION_LITERAL));
        noInstall = o != null && o.getValue();

        log.debug("no install: " + noInstall);

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

            ReleaseMode mode = ReleaseMode.custom;

            if (ReleaseMode.custom.name().equals(crt)) {

                //
                // redundant specification of the "custom" mode, use the next argument as the version label
                //

                if (i == commandLineArguments.size() - 1) {

                    throw new UserErrorException("missing custom release version");
                }

                //
                // the label is the next in line
                //

                commandLineArguments.remove(i);
                crt = commandLineArguments.get(i);
            }

            try {

                mode.setCustomLabel(crt);
            }
            catch(VersionFormatException e) {

                throw new UserErrorException("invalid custom release label \"" + crt + "\"", e);
            }
            setMode(mode);
            commandLineArguments.remove(i);
        }
    }

    @Override
    public void execute(ApplicationRuntime runtime) throws Exception {

        ReleaseApplicationRuntime r = (ReleaseApplicationRuntime)runtime;
        Project p = projectBuilder.build(r.getCurrentDirectory());

        insureInRightDirectory(p);

        if (ReleaseMode.info.equals(mode)) {

            info(r, p);
        }
        else {

            executeReleaseSequence(r, p, mode);
        }
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * In case of custom release, the valid release label can be retrieved as mode.getCustomVersion().
     */
    public ReleaseMode getMode() {

        return mode;

    }

    // Package protected -----------------------------------------------------------------------------------------------

    void setMode(ReleaseMode mode) {

        this.mode = mode;
    }

    void setProjectBuilder(ProjectBuilder b) {

        this.projectBuilder = b;
    }

    // Protected -------------------------------------------------------------------------------------------------------

    boolean isNoTests() {

        return noTests;
    }

    void setNoTests(boolean b) {

        this.noTests = b;
    }

    boolean isNoPush() {

        return noPush;
    }

    void setNoPush(boolean b) {

        this.noPush = b;
    }

    boolean isNoInstall() {

        return noInstall;
    }

    void setNoInstall(boolean b) {

        this.noInstall = b;
    }

    /**
     * Use for testing only. May return null.
     */
    SequenceController getSequenceController() {

        return controller;
    }

    // Private ---------------------------------------------------------------------------------------------------------

    private void info(ReleaseApplicationRuntime r, Project p) throws UserErrorException {

        try {

            Version version = p.getVersion();
            r.info(version.getLiteral());
        }
        catch(VersionFormatException e) {

            throw new UserErrorException(e.getMessage());
        }

    }

    private void executeReleaseSequence(ReleaseApplicationRuntime r, Project p, ReleaseMode rm) throws Exception {

        //
        // transfer execution option as runtime variables to the runtime
        //

        r.setVariableValue(ConfigurationLabels.QUALIFICATION_NO_TESTS, Boolean.toString(isNoTests()));
        r.setVariableValue(ConfigurationLabels.PUBLISH_NO_PUSH, Boolean.toString(isNoPush()));
        r.setVariableValue(ConfigurationLabels.INSTALL_NO_INSTALL, Boolean.toString(isNoInstall()));

        //
        // Install the "known" sequences required by a release into a new controller instance created for this run.
        // The composition of the sequence is influenced by the release command options.
        //

        List<Class<? extends Sequence>> sequenceTypes = getSequenceTypes(isNoInstall());

        controller = new SequenceController(rm, sequenceTypes);

        boolean successfulRelease = false;

        SequenceExecutionContext ctx = null;

        try {

            ctx = controller.execute(r, p);
            successfulRelease = true;
        }
        finally {

            if (!successfulRelease) {

                ctx = controller.undo(r, p);
            }

            r.setLastExecutionContext(ctx);
        }
    }

    /**
     * We look at the project and detect conditions like nort being executed from a sub-module, etc. Throw an
     * UserErrorException with a descriptive message.
     */
    private void insureInRightDirectory(Project p) throws UserErrorException, VersionFormatException {

        Version v = p.getVersion();

        if (v == null) {


            throw new UserErrorException(Files.normalizePath(p.getBaseDirectory().getAbsolutePath()) +
                    " does not seem to contain a valid top-level project POM. Make sure the release process is started from the project directory.");
        }
    }

    // Inner classes ---------------------------------------------------------------------------------------------------

}
