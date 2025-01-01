package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.PropertyBasedJobInclusionStrategy.PropertyBasedJobInclusionStrategyDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

public class PropertyBasedJobInclusionStrategyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Rule
    public TestName testName = new TestName();

    private FreeStyleProject project;
    private String strategyName;
    private PropertyBasedJobInclusionStrategy strategy;
    private DecisionLogger decisionLogger;
    private List<String> loggedMessages;

    @Before
    public void createStrategy() throws Exception {
        strategyName = "testGroup-" + testName.getMethodName();
        strategy = new PropertyBasedJobInclusionStrategy(strategyName);
    }

    @Before
    public void createProject() throws Exception {
        // Name each freestyle project with the name of the test creating it
        project = j.createFreeStyleProject("freestyle-" + testName.getMethodName());
    }

    @Before
    public void createDecisionLogger() throws Exception {
        loggedMessages = new ArrayList<>();
        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                loggedMessages.add(log);
                return this;
            }
        };
    }

    @After
    public void deleteProject() throws Exception {
        project.delete();
    }

    @Test
    public void all() {
        List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
        assertNotNull(jobGroups);
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void getName() {
        assertEquals(strategyName, strategy.getName());
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void contains() {
        boolean result = strategy.contains(decisionLogger, project);
        assertFalse(result); // Assuming the project does not have the required property
        assertThat(loggedMessages, hasItems("No match ..."));
    }

    @Test
    public void getPropertyBasesJobGroups() {
        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void getDisplayNameContainsJobs() {
        PropertyBasedJobInclusionStrategy.PropertyBasedJobInclusionStrategyDescriptor descriptor =
                strategy.getThisDescriptor();
        assertTrue(descriptor.getDisplayName().contains("Jobs"));
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void allReturnsNonNullJobGroups() {
        List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
        assertNotNull(jobGroups);
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals(strategyName, strategy.getName());
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void containsReturnsFalseForProjectWithoutProperty() {
        boolean result = strategy.contains(decisionLogger, project);
        assertFalse(result);
        assertThat(loggedMessages, hasItems("No match ..."));
    }

    @Test
    public void getPropertyBasesJobGroupsReturnsNonNullListBoxModel() {
        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void containsReturnsTrueForProjectWithMatchingJobGroup() throws Exception {
        project.addProperty(new JobInclusionJobProperty(true, strategyName));

        boolean result = strategy.contains(decisionLogger, project);
        assertTrue(result);
        assertThat(
                loggedMessages,
                hasItems(
                        "JobGroup is enabled on job, with JobGroup [" + strategyName + "] ...",
                        "Job is included in JobGroup ..."));
    }

    @Test
    public void containsReturnsFalseForProjectWithNonMatchingJobGroup() throws Exception {
        project.addProperty(new JobInclusionJobProperty(true, "nonMatchingGroup"));

        boolean result = strategy.contains(decisionLogger, project);
        assertFalse(result);
        assertThat(
                loggedMessages,
                hasItems(
                        "JobGroup is enabled on job, with JobGroup [nonMatchingGroup] ...",
                        "Job is not included in JobGroup ..."));
    }

    @Test
    public void containsReturnsFalseWhenCloudBeesFoldersPluginNotEnabled() throws Exception {
        // Simulate CloudBees Folders plugin not enabled
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getThisDescriptor();
        descriptor.cloudbeesFolders = false;

        boolean result = strategy.contains(decisionLogger, project);
        assertFalse(result);
        assertThat(loggedMessages, hasItems("Checking for Job Property inclusion for [" + strategyName + "]..."));
    }

    @Test
    public void getPropertyBasesJobGroupsReturnsEmptyListBoxModelWhenNoJobGroups() {
        // Simulate no job groups
        PriorityConfiguration.get().getJobGroups().clear();

        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
        assertTrue(jobGroups.isEmpty());
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void containsReturnsTrueForProjectWithMatchingPropertyAndCloudBeesFoldersEnabled() throws Exception {
        project.addProperty(new JobInclusionJobProperty(true, strategyName));
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getThisDescriptor();
        descriptor.cloudbeesFolders = true;

        boolean result = strategy.contains(decisionLogger, project);
        assertTrue(result);
        assertThat(
                loggedMessages,
                hasItems(
                        "JobGroup is enabled on job, with JobGroup [" + strategyName + "] ...",
                        "Job is included in JobGroup ..."));
    }

    @Test
    public void containsReturnsFalseForProjectWithNonMatchingPropertyAndCloudBeesFoldersEnabled() throws Exception {
        project.addProperty(new JobInclusionJobProperty(true, "nonMatchingGroup"));
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getThisDescriptor();
        descriptor.cloudbeesFolders = true;

        boolean result = strategy.contains(decisionLogger, project);
        assertFalse(result);
        assertThat(
                loggedMessages,
                hasItems(
                        "JobGroup is enabled on job, with JobGroup [nonMatchingGroup] ...",
                        "Job is not included in JobGroup ..."));
    }

    @Test
    public void containsReturnsFalseForProjectWithoutPropertyAndCloudBeesFoldersEnabled() throws Exception {
        PropertyBasedJobInclusionStrategyDescriptor descriptor = strategy.getThisDescriptor();
        descriptor.cloudbeesFolders = true;

        boolean result = strategy.contains(decisionLogger, project);
        assertFalse(result);
        assertThat(loggedMessages, hasItems("No match ..."));
    }

    @Test
    public void getPropertyBasesJobGroupsReturnsNonNullListBoxModelWhenMultipleJobGroups() {
        PriorityConfiguration.get().getJobGroups().add(createJobGroup("group1", 1));
        PriorityConfiguration.get().getJobGroups().add(createJobGroup("group2", 2));

        ListBoxModel jobGroups = PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
        assertNotNull(jobGroups);
        assertEquals(2, jobGroups.size());
        assertThat(loggedMessages, is(empty()));
    }

    private JobGroup createJobGroup(String description, int priority) {
        JobGroup group = new JobGroup();
        group.setDescription(description);
        group.setPriority(priority);
        group.setJobGroupStrategy(new PropertyBasedJobInclusionStrategy(description));
        return group;
    }
}
