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

import io.novaordis.release.MockConfiguration;
import io.novaordis.release.MockMavenProject;
import io.novaordis.release.MockOS;
import io.novaordis.release.MockReleaseApplicationRuntime;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class BuildSequenceTest extends SequenceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BuildSequenceTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void before() throws Exception {

        System.setProperty("os.class", MockOS.class.getName());
    }

    @After
    public void after() throws Exception {

        ((MockOS) OS.getInstance()).reset();
        System.clearProperty("os.class");
    }

    @Test
    public void osCommandToBuildWithTestsNotConfigured() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        MockMavenProject mp = new MockMavenProject();

        assertNull(mc.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITH_TESTS));

        BuildSequence s = new BuildSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, false, null);
        c.setTestsExecuted(false);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to build with tests was not configured for this project", msg);
        }
    }

    @Test
    public void osCommandToBuildWithoutTestsNotConfigured() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        MockMavenProject mp = new MockMavenProject();

        assertNull(mc.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS));

        BuildSequence s = new BuildSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, false, null);
        c.setTestsExecuted(true);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to build without tests was not configured for this project", msg);
        }
    }

    @Test
    public void buildSuccess_WithTests() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        MockMavenProject mp = new MockMavenProject();

        //
        // instruct the mock OS instance to succeed when building with tests
        //
        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITH_TESTS, "mock build with tests");
        mockOS.addToCommandsThatSucceed("mock build with tests");

        BuildSequence s = new BuildSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, false, null);
        c.setTestsExecuted(false);

        s.execute(c);

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(1, executedCommands.size());
        assertEquals("mock build with tests", executedCommands.get(0));
    }

    @Test
    public void buildSuccess_WithoutTests() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        MockMavenProject mp = new MockMavenProject();

        //
        // instruct the mock OS instance to succeed when building without tests
        //
        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS, "mock build without tests");
        mockOS.addToCommandsThatSucceed("mock build without tests");

        BuildSequence s = new BuildSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, false, null);
        c.setTestsExecuted(true);

        s.execute(c);

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(1, executedCommands.size());
        assertEquals("mock build without tests", executedCommands.get(0));
    }

    @Test
    public void buildFailure() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        MockMavenProject mp = new MockMavenProject();

        //
        // instruct the mock OS instance to fail when building with tests
        //
        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITH_TESTS, "mock build with tests");
        mockOS.addToCommandsThatFail("mock build with tests");

        BuildSequence s = new BuildSequence();

        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, false, null);
        c.setTestsExecuted(false);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("build failed", msg);
        }

        List<String> executedCommands = mockOS.getHistory();
        assertEquals(1, executedCommands.size());
        assertEquals("mock build with tests", executedCommands.get(0));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected BuildSequence getSequenceToTest() throws Exception {

        return new BuildSequence();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
