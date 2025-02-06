package jenkins.advancedqueue.test;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import hudson.tasks.BuildTrigger;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class SortBlockedItemsTest {

    @Test
    @LocalData
    void blockedItemsAreSortedByPriority(JenkinsRule j) throws Exception {
        // Priority value is configured in jenkins.advancedqueue.PrioritySorterConfiguration. Value is 2.
        FreeStyleProject upstreamProject = j.createFreeStyleProject("upstreamProject");
        // Priority value is configured in jenkins.advancedqueue.PrioritySorterConfiguration. Value is 1.
        FreeStyleProject downstreamProject = j.createFreeStyleProject("downstreamProject");
        upstreamProject.setBlockBuildWhenDownstreamBuilding(true);
        downstreamProject.setBlockBuildWhenUpstreamBuilding(true);
        upstreamProject.getPublishersList().add(new BuildTrigger(downstreamProject.getName(), true));
        // Expected build sequence: downstreamProject#1 -> upstreamProject#1 -> downstreamProject#2
        TestRunListener.init(
                new ExpectedItem(downstreamProject.getName(), 1),
                new ExpectedItem(upstreamProject.getName(), 2),
                new ExpectedItem(downstreamProject.getName(), 1));

        j.jenkins.rebuildDependencyGraph();

        // Locking makes sure that the queue sees both builds at the same time even though upstreamProject entered the
        // queue first
        Queue.withLock(() -> {
            j.jenkins.getQueue().schedule2(upstreamProject, 0);
            j.jenkins.getQueue().schedule2(downstreamProject, 0);
            j.jenkins.getQueue().maintain();
        });
        j.waitUntilNoActivity();

        TestRunListener.assertStartedItems();
    }
}
