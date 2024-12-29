package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import hudson.model.Run;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionFolderPropertyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject project;
    private static JobInclusionFolderProperty property;

    @BeforeClass
    public static void createProject() throws Exception {
        project = j.createFreeStyleProject();
        Run r = project.scheduleBuild2(0).get(); // Schedule a build to ensure the queue item is created
        j.assertBuildStatusSuccess(r);
        property = new JobInclusionFolderProperty(true, "testGroup");

        // Ensure the custom QueueSorter is used
        project.getDescriptor();

        Queue.Item queueItem = project.getQueueItem();
        assertNull("Queue.Item should be null", queueItem);
    }

    @Test
    public void testGetJobGroupName() {
        assertEquals("testGroup", property.getJobGroupName());
    }

    @Test
    public void testIsUseJobGroup() {
        assertTrue(property.isUseJobGroup());
    }

    @Test
    public void testGetDescriptor() {
        assertThat(property.getDescriptor().getId(), is(JobInclusionFolderProperty.class.getName()));
    }

    @Test
    public void testDescriptorImpl() {
        JobInclusionFolderProperty.DescriptorImpl descriptor = new JobInclusionFolderProperty.DescriptorImpl();
        assertThat(descriptor.getDisplayName(), is("XXX"));
        assertThat(descriptor.getJobGroups(), is(empty()));
        assertFalse(descriptor.isUsed());
    }

    @Test
    public void testAllJobsJobInclusionStrategy() {
        AllJobsJobInclusionStrategy strategy = new AllJobsJobInclusionStrategy();
        assertTrue(strategy.contains(null, project));
    }
}
