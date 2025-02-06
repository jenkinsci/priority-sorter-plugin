package jenkins.advancedqueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Cause;
import hudson.model.Queue;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.advancedqueue.sorter.SorterStrategy;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import jenkins.advancedqueue.sorter.strategy.MultiBucketStrategy;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PriorityConfigurationPlaceholderTaskHelperTest {

    private static JenkinsRule j;

    private static Queue.Item pipelineItemInQuietPeriod;
    private static DecisionLogger decisionLogger;
    private static List<String> loggedMessages;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        // Start a Pipeline with a quiet period of 37 seconds before it runs
        String pipelineName = "my-pipeline-in-the-quiet-period";
        WorkflowJob pipeline = j.createProject(WorkflowJob.class, pipelineName);
        String pipelineDefinition =
                """
                   node {
                       echo 'Hello from a node'
                       sleep 41
                   }
                """;
        pipeline.setDefinition(new CpsFlowDefinition(pipelineDefinition, true));
        pipeline.scheduleBuild(37, new Cause.UserIdCause());
        pipelineItemInQuietPeriod = findQueueItem(pipelineName);
        assertNotNull(pipelineItemInQuietPeriod, "Pipeline in quiet period not in Queue");
        // Check the item is blocked due to the 37 second quiet period
        assertThat(
                pipelineItemInQuietPeriod.getCauseOfBlockage().getShortDescription(),
                startsWith("In the quiet period."));

        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                loggedMessages.add(log);
                return this;
            }
        };
    }

    private static Queue.Item findQueueItem(String name) {
        Queue.Item found = null;
        Queue.Item[] items = j.jenkins.getQueue().getItems();
        for (Queue.Item item : items) {
            if (item.getDisplayName().equals(name)) {
                found = item;
            }
        }
        return found;
    }

    @BeforeEach
    void beforeEach() throws Exception {
        loggedMessages = new ArrayList<>();
    }

    @Test
    void testGetPriorityAssignsGlobalDefault() {
        PriorityConfiguration configuration = new PriorityConfiguration();
        PriorityConfigurationCallbackImpl callback = new PriorityConfigurationCallbackImpl();
        assertThat(callback.getPrioritySelection(), is(-1)); // Before callback is used
        configuration.getPriority(pipelineItemInQuietPeriod, callback);
        assertThat(loggedMessages, hasItem("Assigning global default priority"));
        assertThat(callback.getPrioritySelection(), is(MultiBucketStrategy.DEFAULT_PRIORITY)); // After callback is used
    }

    @Test
    void testIsPlaceholderTask() {
        PriorityConfigurationPlaceholderTaskHelper helper = new PriorityConfigurationPlaceholderTaskHelper();
        // Pipeline task is not a placeholder task
        assertFalse(helper.isPlaceholderTask(pipelineItemInQuietPeriod.getTask()));
    }

    @Test
    void testGetPriority() {
        // Could not find an easy way to generate a placeholder task
        // Use a mock object for better test coverage
        ExecutorStepExecution.PlaceholderTask task = mock(ExecutorStepExecution.PlaceholderTask.class);
        when(task.getOwnerTask()).thenReturn(pipelineItemInQuietPeriod.getTask());

        PriorityConfigurationPlaceholderTaskHelper helper = new PriorityConfigurationPlaceholderTaskHelper();
        PriorityConfigurationCallbackImpl callback = new PriorityConfigurationCallbackImpl();
        assertThat(callback.getPrioritySelection(), is(-1)); // Before callback is used
        callback.expectPrioritySelection(3);
        PriorityConfigurationCallback result = helper.getPriority(task, callback);
        assertNotNull(result);

        // Verify that the default priority from the MultiBucketStrategy is returned by the strategy for the given item
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITY, callback.getPrioritySelection());
    }

    @Test
    void testGetPriorityNonJobTask() {
        // Could not find an easy way to generate a placeholder task
        // Use a mock object for better test coverage
        ExecutorStepExecution.PlaceholderTask task = mock(ExecutorStepExecution.PlaceholderTask.class);
        Queue.Task ownerTask = mock(Queue.Task.class);
        when(task.getOwnerTask()).thenReturn(ownerTask);

        // Use a custom sorter strategy
        SorterStrategyImpl strategy = new SorterStrategyImpl();
        PrioritySorterConfiguration.get().setStrategy(strategy);

        PriorityConfigurationPlaceholderTaskHelper helper = new PriorityConfigurationPlaceholderTaskHelper();
        PriorityConfigurationCallbackImpl callback = new PriorityConfigurationCallbackImpl();
        assertThat(callback.getPrioritySelection(), is(-1)); // Before callback is used
        // Check that default priority of custom strategy is different than system-wide default priority
        assertThat(strategy.getDefaultPriority(), is(not(MultiBucketStrategy.DEFAULT_PRIORITY)));
        callback.expectPrioritySelection(strategy.getDefaultPriority());
        PriorityConfigurationCallback result = helper.getPriority(task, callback);
        assertNotNull(result);

        // Verify that the default priority from the strategy is returned
        assertEquals(strategy.getDefaultPriority(), callback.getPrioritySelection());
    }

    @Test
    void testIsPlaceholderTaskUsed() {
        assertTrue(PriorityConfigurationPlaceholderTaskHelper.isPlaceholderTaskUsed());
    }

    private static class PriorityConfigurationCallbackImpl implements PriorityConfigurationCallback {

        private int prioritySelection = -1;
        private int expectedPrioritySelection = -1;

        public PriorityConfigurationCallbackImpl() {}

        private int getPrioritySelection() {
            return prioritySelection;
        }

        @Override
        public PriorityConfigurationCallback setPrioritySelection(int priority) {
            prioritySelection = priority;
            if (expectedPrioritySelection != -1) {
                assertThat(priority, is(expectedPrioritySelection));
            }
            return this;
        }

        @Override
        public PriorityConfigurationCallback setPrioritySelection(
                int priority, int jobGroupId, PriorityStrategy reason) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public PriorityConfigurationCallback setPrioritySelection(
                int priority, long sortAsInQueueSince, int jobGroupId, PriorityStrategy reason) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public DecisionLogger addDecisionLog(int indent, String log) {
            decisionLogger.addDecisionLog(indent, log);
            return decisionLogger;
        }

        private void expectPrioritySelection(int priority) {
            expectedPrioritySelection = priority;
        }
    }

    /* A sorter strategy that is intentionally different than the default */
    private static class SorterStrategyImpl extends SorterStrategy {

        private final int NUMBER_OF_PRIORITIES = 4 + MultiBucketStrategy.DEFAULT_PRIORITIES_NUMBER;

        public SorterStrategyImpl() {}

        @Override
        public SorterStrategyCallback onNewItem(Queue.Item item, SorterStrategyCallback weightCallback) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getNumberOfPriorities() {
            return NUMBER_OF_PRIORITIES;
        }

        @Override
        public int getDefaultPriority() {
            return NUMBER_OF_PRIORITIES / 2;
        }
    }
}
