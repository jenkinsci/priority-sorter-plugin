package jenkins.advancedqueue.priority.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.StaplerRequest2;

@WithJenkins
class PriorityJobPropertyTest {

    private static JenkinsRule j;

    private String testName;

    private static PriorityJobProperty property;
    private static PriorityJobProperty.DescriptorImpl descriptor;

    private static final int PRIORITY = 7;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        // Initialize PrioritySorterConfiguration
        PrioritySorterConfiguration.get().load();
        property = new PriorityJobProperty(true, PRIORITY);
        descriptor = property.getDescriptor();
    }

    @BeforeEach
    void beforeEach(TestInfo info) throws Exception {
        testName = info.getTestMethod().orElseThrow().getName();
    }

    @Test
    void priorityJobProperty_returnsCorrectPriority() {
        assertEquals(PRIORITY, property.getPriority());
    }

    @Test
    void priorityJobProperty_returnsCorrectUseJobPriority() {
        assertTrue(property.getUseJobPriority());
    }

    @Test
    void priorityJobProperty_reconfigureNullOnEmpty() throws Descriptor.FormException {
        StaplerRequest2 req = mock(StaplerRequest2.class);
        assertNull(property.reconfigure(req, new JSONObject()));
    }

    @Test
    void descriptorImpl_getDefaultReturnsDefaultPriority() {
        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), descriptor.getDefault());
    }

    @Test
    void descriptorImpl_getPrioritiesReturnsNonEmptyList() {
        assertFalse(descriptor.getPriorities().isEmpty());
    }

    private final Random random = new Random();

    private JobGroup createJobGroup(String viewName) {
        JobGroup jobGroup = new JobGroup();
        jobGroup.setDescription("testGroup-" + testName);
        jobGroup.setRunExclusive(random.nextBoolean());
        jobGroup.setId(random.nextInt());
        jobGroup.setJobGroupStrategy(new ViewBasedJobInclusionStrategy(viewName));
        return jobGroup;
    }

    @Test
    void isUsedWhenViewExists() throws IOException {
        // Create a new FreeStyleProject
        FreeStyleProject project = j.createFreeStyleProject();

        // Create a new view named "existingView"
        ListView view = new ListView("existingView", j.jenkins);
        j.jenkins.addView(view);

        // Add the project to the view
        view.add(project);

        // Verify that the view was created successfully
        assertNotNull(j.jenkins.getView("existingView"));

        // Set up the PriorityJobProperty.DescriptorImpl
        PriorityConfiguration configuration = PriorityConfiguration.get();
        List<JobGroup> jobGroups = configuration.getJobGroups();
        JobGroup jobGroup = createJobGroup(view.getViewName());
        jobGroup.setUsePriorityStrategies(true);

        // Add a PriorityStrategyHolder with a JobPropertyStrategy to the JobGroup
        JobPropertyStrategy jobPropertyStrategy = new JobPropertyStrategy();
        JobGroup.PriorityStrategyHolder priorityStrategyHolder =
                new JobGroup.PriorityStrategyHolder(1, jobPropertyStrategy);
        jobGroup.getPriorityStrategies().add(priorityStrategyHolder);

        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);

        // Assert the strategy is used when priority strategies are used and view exists
        assertTrue(descriptor.isUsed(project));

        // Replace the jobGroup with one that does not use priority strategies
        jobGroups.remove(jobGroup);
        jobGroup.setUsePriorityStrategies(false);
        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);

        // Assert the strategy is not used when priority strategies are not used even if view exists
        assertFalse(descriptor.isUsed(project));
    }

    @Test
    void isUsedWhenViewDoesNotExist() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject();
        PriorityConfiguration configuration = PriorityConfiguration.get();
        List<JobGroup> jobGroups = configuration.getJobGroups();
        JobGroup jobGroup = createJobGroup("intentionally-non-existent-view");

        // Use priority strategies does not make a non-existing view used
        jobGroup.setUsePriorityStrategies(true);
        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);
        assertFalse(descriptor.isUsed(project));

        // Not using priority strategies does not make a non-existing view used
        // Replace the jobGroup with one that does not use priority strategies
        jobGroups.remove(jobGroup);
        jobGroup.setUsePriorityStrategies(false);
        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);
        assertFalse(descriptor.isUsed(project));
    }
}
