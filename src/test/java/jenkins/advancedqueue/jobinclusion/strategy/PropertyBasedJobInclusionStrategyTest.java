package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import java.util.List;
import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.PropertyBasedJobInclusionStrategy.PropertyBasedJobInclusionStrategyDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PropertyBasedJobInclusionStrategyTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private FreeStyleProject j;
    private PropertyBasedJobInclusionStrategy strategy;
    private static DecisionLogger decisionLogger;

    @Before
    public void setUp() throws Exception {
        j = jenkinsRule.createFreeStyleProject();
        strategy = new PropertyBasedJobInclusionStrategy("testGroup");
        decisionLogger = new DecisionLogger() {
            /**
             * @param indent
             * @param log
             * @return
             */
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        j.delete();
    }

    @Test
    public void getDescriptor() {
        PropertyBasedJobInclusionStrategy.PropertyBasedJobInclusionStrategyDescriptor descriptor =
                strategy.getDescriptor();
        assertNotNull(descriptor);
        assertTrue(descriptor.getDisplayName().contains("Jobs"));
    }

    @Test
    public void all() {
        List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
        assertNotNull(jobGroups);
    }

    @Test
    public void getName() {
        assertEquals("testGroup", strategy.getName());
    }

    @Test
    public void contains() {

        boolean result = strategy.contains(decisionLogger, j);
        assertFalse(result); // Assuming the project does not have the required property
    }

    @Test
    public void getPropertyBasesJobGroups() {
        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
    }

    @Test
    public void getDescriptorReturnsNonNullDescriptor() {
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getDescriptor();
        assertNotNull(descriptor);
    }

    @Test
    public void getDescriptorDisplayNameContainsJobs() {
        PropertyBasedJobInclusionStrategy.PropertyBasedJobInclusionStrategyDescriptor descriptor =
                strategy.getDescriptor();
        assertTrue(descriptor.getDisplayName().contains("Jobs"));
    }

    @Test
    public void allReturnsNonNullJobGroups() {
        List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
        assertNotNull(jobGroups);
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("testGroup", strategy.getName());
    }

    @Test
    public void containsReturnsFalseForProjectWithoutProperty() {
        boolean result = strategy.contains(decisionLogger, j);
        assertFalse(result);
    }

    @Test
    public void getPropertyBasesJobGroupsReturnsNonNullListBoxModel() {
        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
    }

    @Test
    public void containsReturnsTrueForProjectWithMatchingJobGroup() throws Exception {
        j.addProperty(new JobInclusionJobProperty(true, "testGroup"));

        boolean result = strategy.contains(decisionLogger, j);
        assertTrue(result);
    }

    @Test
    public void containsReturnsFalseForProjectWithNonMatchingJobGroup() throws Exception {
        j.addProperty(new JobInclusionJobProperty(true, "nonMatchingGroup"));

        boolean result = strategy.contains(decisionLogger, j);
        assertFalse(result);
    }

    @Test
    public void containsReturnsFalseWhenCloudBeesFoldersPluginNotEnabled() throws Exception {
        // Simulate CloudBees Folders plugin not enabled
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getDescriptor();
        descriptor.cloudbeesFolders = false;

        boolean result = strategy.contains(decisionLogger, j);
        assertFalse(result);
    }

    @Test
    public void getPropertyBasesJobGroupsReturnsEmptyListBoxModelWhenNoJobGroups() {
        // Simulate no job groups
        PriorityConfiguration.get().getJobGroups().clear();

        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
        assertTrue(jobGroups.isEmpty());
    }

    @Test
    public void containsReturnsTrueForProjectWithMatchingPropertyAndCloudBeesFoldersEnabled() throws Exception {
        j.addProperty(new JobInclusionJobProperty(true, "testGroup"));
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getDescriptor();
        descriptor.cloudbeesFolders = true;

        boolean result = strategy.contains(decisionLogger, j);
        assertTrue(result);
    }

    @Test
    public void containsReturnsFalseForProjectWithNonMatchingPropertyAndCloudBeesFoldersEnabled() throws Exception {
        j.addProperty(new JobInclusionJobProperty(true, "nonMatchingGroup"));
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getDescriptor();
        descriptor.cloudbeesFolders = true;

        boolean result = strategy.contains(decisionLogger, j);
        assertFalse(result);
    }

    @Test
    public void containsReturnsFalseForProjectWithoutPropertyAndCloudBeesFoldersEnabled() throws Exception {
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getDescriptor();
        descriptor.cloudbeesFolders = true;

        boolean result = strategy.contains(decisionLogger, j);
        assertFalse(result);
    }

    @Test
    public void getPropertyBasesJobGroupsReturnsNonNullListBoxModelWhenMultipleJobGroups() {
        // Simulate multiple job groups
        PriorityConfiguration.get()
                .getJobGroups()
                .add(new JobGroup("group1", 1, new PropertyBasedJobInclusionStrategy("group1")));
        PriorityConfiguration.get()
                .getJobGroups()
                .add(new JobGroup("group2", 2, new PropertyBasedJobInclusionStrategy("group2")));

        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
        assertEquals(2, jobGroups.size());
    }
}
