/*
 * The MIT License
 *
 * Copyright 2023 Mark Waite.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.advancedqueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

import io.jenkins.plugins.casc.misc.RoundTripAbstractTest;
import jenkins.advancedqueue.sorter.strategy.AbsoluteStrategy;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class SimpleJCasCTest extends RoundTripAbstractTest {

    @Override
    protected void assertConfiguredAsExpected(RestartableJenkinsRule j, String configContent) {
        PrioritySorterConfiguration globalConfig = PrioritySorterConfiguration.get();
        assertFalse("Non-admins cannot edit priority", globalConfig.getOnlyAdminsMayEditPriorityConfiguration());
        assertThat("Wrong strategy class", globalConfig.getStrategy().getClass(), is(AbsoluteStrategy.class));
        assertThat("Wrong number of priorities", globalConfig.getStrategy().getNumberOfPriorities(), is(5));
        assertThat("Wrong default priority", globalConfig.getStrategy().getDefaultPriority(), is(3));

        PriorityConfiguration config = PriorityConfiguration.get();
        assertThat("Wrong number of job groups", config.getJobGroups().size(), is(1));
    }

    @Override
    protected String stringInLogExpected() {
        return ".description = Group 1 - default priority";
    }

    @Override
    protected String configResource() {
        return "simple-jcasc.yaml";
    }
}
