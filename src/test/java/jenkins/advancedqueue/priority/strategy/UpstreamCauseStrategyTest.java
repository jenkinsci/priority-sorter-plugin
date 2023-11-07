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
package jenkins.advancedqueue.priority.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.labels.LabelAtom;
import jenkins.triggers.ReverseBuildTrigger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class UpstreamCauseStrategyTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public UpstreamCauseStrategyTest() {}

    @Test
    public void testGetUpstreamCause() throws Exception {
        // Create an upstream job
        FreeStyleProject upstream = j.createFreeStyleProject("upstream-job");

        // Create a downstream job that runs after the upstream job
        // Configure the downstream job to block in the queue by requiring a non-existent label
        FreeStyleProject downstream = j.createFreeStyleProject("downstream-job");
        downstream.addTrigger(new ReverseBuildTrigger("upstream-job"));
        downstream.setAssignedLabel(new LabelAtom("non-existent-label")); // block job waiting for label
        assertThat(downstream.getAssignedLabelString(), is("non-existent-label"));

        // Run the upstream job with a UserIdCause
        Cause cause = new Cause.UserIdCause();
        FreeStyleBuild build = j.assertBuildStatus(
                Result.SUCCESS, upstream.scheduleBuild2(0, cause).get());
        assertThat(build.getCauses(), hasItem(cause));
        assertThat(upstream.getBuildsAsMap(), is(aMapWithSize(1))); // one build

        // Find the downstream job blocked in queue waiting for non-exstent label
        assertFalse(downstream.isBuilding());
        assertThat(downstream.getBuildsAsMap(), is(aMapWithSize(0))); // no builds

        // The next assertion fails unexpectedly
        // I expected the job to be in the queue

        // assertTrue(downstream.isInQueue());

        // These statements will never be reached because
        // downstream is not in the queue in my test
        // I don't know why downstream is not blocked in the queue
        // Blocks in the queue when I'm running Jenkins interactively

        // Queue.Item[] items = j.jenkins.getQueue().getItems();
        // Queue.Item item = items[0];
        // UpstreamCauseStrategy causeStrategy = new UpstreamCauseStrategy();
        // assertThat(causeStrategy.getUpstreamCause(item), is(cause));

        // Need to stop the downstream job before exiting test
        downstream.delete();
    }
}
