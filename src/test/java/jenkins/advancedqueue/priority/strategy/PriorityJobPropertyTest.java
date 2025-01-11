package jenkins.advancedqueue.priority.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;

public class PriorityJobPropertyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Rule
    public TestName testName = new TestName();

    private static PriorityJobProperty property;
    private static PriorityJobProperty.DescriptorImpl descriptor;

    private static final int PRIORITY = 7;

    @BeforeClass
    public static void setUp() throws IOException {
        // Initialize PrioritySorterConfiguration
        PrioritySorterConfiguration.get().load();
        property = new PriorityJobProperty(true, PRIORITY);
        descriptor = property.getDescriptor();
    }

    @Test
    public void priorityJobProperty_returnsCorrectPriority() {
        assertEquals(PRIORITY, property.getPriority());
    }

    @Test
    public void priorityJobProperty_returnsCorrectUseJobPriority() {
        assertTrue(property.getUseJobPriority());
    }

    @Test
    public void priorityJobProperty_reconfigureNullOnEmpty() throws Descriptor.FormException {
        StaplerRequest req = mock(StaplerRequest.class);
        assertNull(property.reconfigure(req, new JSONObject()));
    }

    @Test
    public void descriptorImpl_getDefaultReturnsDefaultPriority() {
        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), descriptor.getDefault());
    }

    @Test
    public void descriptorImpl_getPrioritiesReturnsNonEmptyList() {
        assertFalse(descriptor.getPriorities().isEmpty());
    }

    private final Random random = new Random();

    private JobGroup createJobGroup(String viewName) {
        JobGroup jobGroup = new JobGroup();
        jobGroup.setDescription("testGroup-" + testName.getMethodName());
        jobGroup.setRunExclusive(random.nextBoolean());
        jobGroup.setUsePriorityStrategies(random.nextBoolean());
        jobGroup.setId(random.nextInt());
        jobGroup.setJobGroupStrategy(new ViewBasedJobInclusionStrategy(viewName));
        return jobGroup;
    }

    @Test
    public void descriptorImpl_isUsedReturnsTrueWhenJobGroupUsesPriorityStrategies() throws IOException {
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

        // Assert the descriptor is used
        assertTrue(descriptor.isUsed(project));
    }

    @Test
    public void descriptorImpl_isUsedReturnsFalseWhenJobGroupDoesNotUsePriorityStrategies() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject();
        PriorityConfiguration configuration = PriorityConfiguration.get();
        List<JobGroup> jobGroups = configuration.getJobGroups();
        JobGroup jobGroup = createJobGroup("defaultView");
        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);
        assertFalse(descriptor.isUsed(project));
    }
}
