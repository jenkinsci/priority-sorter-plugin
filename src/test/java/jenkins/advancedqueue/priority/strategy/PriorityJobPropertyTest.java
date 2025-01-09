package jenkins.advancedqueue.priority.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import java.io.IOException;
import java.util.List;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;

public class PriorityJobPropertyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static PriorityJobProperty property;
    private static StaplerRequest req;
    private static PriorityJobProperty.DescriptorImpl descriptor;
    private static FreeStyleProject project;

    @BeforeClass
    public static void setUp() throws IOException {
        project = j.createFreeStyleProject();

        property = new PriorityJobProperty(true, 7);
        req = mock(StaplerRequest.class);
        descriptor = new PriorityJobProperty.DescriptorImpl();
    }

    @Test
    public void priorityJobProperty_returnsCorrectPriority() {
        assertEquals(7, property.getPriority());
    }

    @Test
    public void priorityJobProperty_returnsCorrectUseJobPriority() {
        assertTrue(property.getUseJobPriority());
    }

    @Test
    public void priorityJobProperty_reconfigureReturnsSameInstance() throws Descriptor.FormException {
        JSONObject form = new JSONObject();
        assertEquals(property, property.reconfigure(req, form));
    }

    @Test
    public void descriptorImpl_getDefaultReturnsDefaultPriority() {
        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), descriptor.getDefault());
    }

    @Test
    public void descriptorImpl_getPrioritiesReturnsNonEmptyList() {
        assertFalse(descriptor.getPriorities().isEmpty());
    }

    @Test
    public void descriptorImpl_isUsedReturnsTrueWhenJobGroupUsesPriorityStrategies() throws IOException {
        // Initialize PrioritySorterConfiguration
        PrioritySorterConfiguration.get().load();

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
        descriptor = new PriorityJobProperty.DescriptorImpl();
        PriorityConfiguration configuration = PriorityConfiguration.get();
        List<JobGroup> jobGroups = configuration.getJobGroups();
        JobGroup jobGroup = new JobGroup();
        jobGroup.setDescription("testGroup");
        jobGroup.setRunExclusive(true);
        jobGroup.setUsePriorityStrategies(true);
        jobGroup.setId(1);
        jobGroup.setJobGroupStrategy(new ViewBasedJobInclusionStrategy("existingView")); // Use the newly created view

        // Add a PriorityStrategyHolder with a JobPropertyStrategy to the JobGroup
        JobPropertyStrategy jobPropertyStrategy = new JobPropertyStrategy();
        JobGroup.PriorityStrategyHolder priorityStrategyHolder =
                new JobGroup.PriorityStrategyHolder(1, jobPropertyStrategy);
        jobGroup.getPriorityStrategies().add(priorityStrategyHolder);

        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);

        // Print the strategy and assert the descriptor is used
        assertTrue(descriptor.isUsed(project));
    }

    @Test
    public void descriptorImpl_isUsedReturnsFalseWhenJobGroupDoesNotUsePriorityStrategies() throws IOException {
        descriptor = new PriorityJobProperty.DescriptorImpl();
        PriorityConfiguration configuration = PriorityConfiguration.get();
        List<JobGroup> jobGroups = configuration.getJobGroups();
        JobGroup jobGroup = new JobGroup();
        jobGroup.setDescription("testGroup");
        jobGroup.setRunExclusive(false);
        jobGroup.setUsePriorityStrategies(false);
        jobGroup.setId(1);
        jobGroup.setJobGroupStrategy(new ViewBasedJobInclusionStrategy("defaultView")); // Set a default strategy
        jobGroups.add(jobGroup);
        configuration.setJobGroups(jobGroups);
        assertFalse(descriptor.isUsed(project));
    }
}
