package jenkins.advancedqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.advancedqueue.priority.PriorityStrategy;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobGroupTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private static JobInclusionStrategy strategy;
    private static PriorityStrategy priorityStrategy;
    private static JobGroup jobGroup = new JobGroup();

    @BeforeClass
    public static void makeStrategy() {
        strategy = new JobInclusionStrategy() {
            /**
             * @param decisionLogger
             * @param job
             * @return
             */
            @Override
            public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job) {
                return false;
            }
        };
    }

    @BeforeClass
    public static void makePriorityStrategy() {
        priorityStrategy = new PriorityStrategy() {
            /**
             * @return
             */
            @Override
            public Descriptor<PriorityStrategy> getDescriptor() {
                return null;
            }

            /**
             * @param item the {@link Queue.Item} to check
             * @return
             */
            @Override
            public boolean isApplicable(Queue.Item item) {
                return false;
            }

            /**
             * @param item the {@link Queue.Item} to check
             * @return
             */
            @Override
            public int getPriority(Queue.Item item) {
                return 0;
            }

            /**
             * @param oldNumberOfPriorities
             * @param newNumberOfPriorities
             */
            @Override
            public void numberPrioritiesUpdates(int oldNumberOfPriorities, int newNumberOfPriorities) {}
        };
    }

    @BeforeClass
    public static void setUp() throws IOException {

        jobGroup = new JobGroup();
    }

    @Test
    public void getIdReturnsCorrectValue() {
        jobGroup.setId(1);
        assertEquals(1, jobGroup.getId());
    }

    @Test
    public void setIdUpdatesValue() {
        jobGroup.setId(2);
        assertEquals(2, jobGroup.getId());
    }

    @Test
    public void getDescriptionReturnsCorrectValue() {
        jobGroup.setDescription("Test Description");
        assertEquals("Test Description", jobGroup.getDescription());
    }

    @Test
    public void setDescriptionUpdatesValue() {
        jobGroup.setDescription("New Description");
        assertEquals("New Description", jobGroup.getDescription());
    }

    @Test
    public void getPriorityReturnsCorrectValue() {
        jobGroup.setPriority(3);
        assertEquals(3, jobGroup.getPriority());
    }

    @Test
    public void setPriorityUpdatesValue() {
        jobGroup.setPriority(4);
        assertEquals(4, jobGroup.getPriority());
    }

    @Test
    public void jobGroupStrategyReturnsCorrectStrategy() {

        jobGroup.setJobGroupStrategy(strategy);
        assertEquals(strategy, jobGroup.getJobGroupStrategy());
    }

    @Test
    public void runExclusiveReturnsCorrectValue() {
        jobGroup.setRunExclusive(true);
        assertTrue(jobGroup.isRunExclusive());
    }

    @Test
    public void setRunExclusiveUpdatesValue() {
        jobGroup.setRunExclusive(false);
        assertFalse(jobGroup.isRunExclusive());
    }

    @Test
    public void usePriorityStrategiesReturnsCorrectValue() {
        jobGroup.setUsePriorityStrategies(true);
        assertTrue(jobGroup.isUsePriorityStrategies());
    }

    @Test
    public void setUsePriorityStrategiesUpdatesValue() {
        jobGroup.setUsePriorityStrategies(false);
        assertFalse(jobGroup.isUsePriorityStrategies());
    }

    @Test
    public void priorityStrategiesReturnsCorrectList() {

        JobGroup.PriorityStrategyHolder holder = new JobGroup.PriorityStrategyHolder(1, priorityStrategy);
        List<JobGroup.PriorityStrategyHolder> strategies = new ArrayList<>();
        strategies.add(holder);
        jobGroup.setPriorityStrategies(strategies);
        assertEquals(strategies, jobGroup.getPriorityStrategies());
    }

    @Test
    public void setPriorityStrategiesUpdatesList() {
        JobGroup.PriorityStrategyHolder holder = new JobGroup.PriorityStrategyHolder(2, priorityStrategy);
        List<JobGroup.PriorityStrategyHolder> strategies = new ArrayList<>();
        strategies.add(holder);
        jobGroup.setPriorityStrategies(strategies);
        assertEquals(strategies, jobGroup.getPriorityStrategies());
    }

    @Test
    public void jobGroupIntegrationWithJenkins() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();

        Queue.Executable executable =
                Queue.getInstance().schedule(project, 0).getFuture().get();
        assertNotNull("Queue executable should not be null", executable);

        jobGroup.setId(1);
        jobGroup.setDescription("Integration Test");
        jobGroup.setPriority(5);
        jobGroup.setRunExclusive(true);
        jobGroup.setUsePriorityStrategies(true);
        assertEquals(1, jobGroup.getId());
        assertEquals("Integration Test", jobGroup.getDescription());
        assertEquals(5, jobGroup.getPriority());
        assertTrue(jobGroup.isRunExclusive());
        assertTrue(jobGroup.isUsePriorityStrategies());
    }
}
