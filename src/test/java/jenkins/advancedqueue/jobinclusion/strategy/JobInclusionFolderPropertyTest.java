package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import jenkins.advancedqueue.jobrestrictions.PrioritySorterRestriction;
import jenkins.advancedqueue.sorter.ItemInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionFolderPropertyTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private static ItemInfo itemInfo;
    private PrioritySorterRestriction restriction;
    private static final int LOWER_PRIORITY = 1;
    private static FreeStyleProject j;
    private static JobInclusionFolderProperty property;

    @Before
    public void setUp() throws Exception {
        j = jenkinsRule.createFreeStyleProject();
        j.scheduleBuild2(0).get(); // Schedule a build to ensure the queue item is created
        property = new JobInclusionFolderProperty(true, "testGroup");

        // Ensure the custom QueueSorter is used
        j.getDescriptor();

        Queue.Item queueItem = j.getQueueItem();
        assertNull("Queue.Item should be null", queueItem);

        if (queueItem != null) {
            itemInfo = new ItemInfo(queueItem, LOWER_PRIORITY);
        }
    }

    @After
    public void tearDown() throws Exception {}

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
        assertNotNull(property.getDescriptor());
    }

    @Test
    public void testDescriptorImpl() {
        JobInclusionFolderProperty.DescriptorImpl descriptor = new JobInclusionFolderProperty.DescriptorImpl();
        assertEquals("XXX", descriptor.getDisplayName());
        assertNotNull(descriptor.getJobGroups());
        assertFalse(descriptor.isUsed());
    }

    @Test
    public void testAllJobsJobInclusionStrategy() {
        AllJobsJobInclusionStrategy strategy = new AllJobsJobInclusionStrategy();
        assertTrue(strategy.contains(null, j));
    }
}
