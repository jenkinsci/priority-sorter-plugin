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
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.jenkins.plugins.casc.misc.junit.jupiter.AbstractRoundTripTest;
import java.util.List;
import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.advancedqueue.sorter.strategy.AbsoluteStrategy;
import org.jvnet.hudson.test.JenkinsRule;

public class JCasCFivePrioritiesTest extends AbstractRoundTripTest {

    @Override
    protected void assertConfiguredAsExpected(JenkinsRule j, String configContent) {
        PrioritySorterConfiguration globalConfig = PrioritySorterConfiguration.get();
        assertFalse(globalConfig.getOnlyAdminsMayEditPriorityConfiguration(), "Non-admins cannot edit priority");
        assertThat("Wrong strategy class", globalConfig.getStrategy().getClass(), is(AbsoluteStrategy.class));
        assertThat("Wrong number of priorities", globalConfig.getStrategy().getNumberOfPriorities(), is(5));
        assertThat("Wrong default priority", globalConfig.getStrategy().getDefaultPriority(), is(3));

        PriorityConfiguration config = PriorityConfiguration.get();
        List<JobGroup> jobGroups = config.getJobGroups();
        assertThat("Wrong number of job groups", jobGroups.size(), is(5));
        assertThat(jobGroups.get(0).getDescription(), is("Group 1 - priority five"));
        assertThat(jobGroups.get(1).getDescription(), is("Group 2 - priority four"));
        assertThat(jobGroups.get(2).getDescription(), is("Group 3 - priority three"));
        assertThat(jobGroups.get(3).getDescription(), is("Group 4 - priority two"));
        assertThat(jobGroups.get(4).getDescription(), is("Group 5 - priority one"));

        List<JobGroup.PriorityStrategyHolder> strategies0 = jobGroups.get(0).getPriorityStrategies();
        PriorityStrategy strategy0 = strategies0.get(0).getPriorityStrategy();
        assertThat(strategy0.getDescriptor().getDisplayName(), is("Job Triggered by a user"));

        List<JobGroup.PriorityStrategyHolder> strategies1 = jobGroups.get(1).getPriorityStrategies();
        PriorityStrategy strategy1 = strategies1.get(0).getPriorityStrategy();
        assertThat(strategy1.getDescriptor().getDisplayName(), is("Job Triggered by an upstream build"));

        List<JobGroup.PriorityStrategyHolder> strategies2 = jobGroups.get(2).getPriorityStrategies();
        PriorityStrategy strategy2 = strategies2.get(0).getPriorityStrategy();
        assertThat(strategy2.getDescriptor().getDisplayName(), is("Job Triggered from CLI"));

        List<JobGroup.PriorityStrategyHolder> strategies3 = jobGroups.get(3).getPriorityStrategies();
        PriorityStrategy strategy3 = strategies3.get(0).getPriorityStrategy();
        assertThat(strategy3.getDescriptor().getDisplayName(), is("Using the Jobs Health"));

        List<JobGroup.PriorityStrategyHolder> strategies4 = jobGroups.get(4).getPriorityStrategies();
        PriorityStrategy strategy4 = strategies4.get(0).getPriorityStrategy();
        assertThat(strategy4.getDescriptor().getDisplayName(), is("Take the priority from property on the job"));
    }

    @Override
    protected String stringInLogExpected() {
        return ".description = Group 5 - priority one";
    }

    @Override
    protected String configResource() {
        return "five-priorities-jcasc.yaml";
    }
}
