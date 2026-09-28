package jenkins.advancedqueue.priority.strategy;

import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Surefire 3.6.0 changed the order of execution of unit tests.  That
 * exposed an order dependency in the tests, so these tests were split
 * to a separate source file.  That was simpler than identifying the
 * cause of the order dependency in the tests.
 */
@WithJenkins
class PriorityJobProperty2Test {

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
}
