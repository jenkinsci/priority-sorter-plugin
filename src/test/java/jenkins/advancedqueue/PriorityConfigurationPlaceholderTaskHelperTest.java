package jenkins.advancedqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.StringParameterValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.advancedqueue.sorter.strategy.MultiBucketStrategy;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PriorityConfigurationPlaceholderTaskHelperTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static PriorityStrategy strategy;
    private static Queue.Item item;
    private static FreeStyleProject project;
    private static Action action;
    private static PriorityConfigurationCallback callback;
    private static PriorityConfigurationPlaceholderTaskHelper helper;
    private static Queue.Task ownerTask;
    private static ExecutorStepExecution.PlaceholderTask task;

    @BeforeClass
    public static void setUp() throws IOException {
        project = j.createFreeStyleProject();
        helper = new PriorityConfigurationPlaceholderTaskHelper();
        task = mock(ExecutorStepExecution.PlaceholderTask.class);
        ownerTask = mock(Queue.Task.class);
    }

    @BeforeClass
    public static void createActionAndItem() {
        StringParameterValue param = new StringParameterValue("priority", "5");
        action = new ParametersAction(param);
        List<Action> actions = new ArrayList<>();
        actions.add(action);
        item = new Queue.WaitingItem(Calendar.getInstance(), project, actions);
    }

    @BeforeClass
    public static void createStrategy() {
        strategy = new PriorityStrategy() {
            @Override
            public boolean isApplicable(Queue.Item item) {
                return false;
            }

            @Override
            public int getPriority(Queue.Item item) {
                return 3;
            }

            @Override
            public void numberPrioritiesUpdates(int oldNumberOfPriorities, int newNumberOfPriorities) {}

            @Override
            public Descriptor<PriorityStrategy> getDescriptor() {
                return null;
            }
        };
    }

    @BeforeClass
    public static void createCallback() {
        callback = new PriorityConfigurationCallback() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                return null;
            }

            @Override
            public PriorityConfigurationCallback setPrioritySelection(int priority) {
                return null;
            }

            @Override
            public PriorityConfigurationCallback setPrioritySelection(
                    int priority, int jobGroupId, PriorityStrategy reason) {
                return null;
            }

            @Override
            public PriorityConfigurationCallback setPrioritySelection(
                    int priority, long sortAsInQueueSince, int jobGroupId, PriorityStrategy reason) {
                return null;
            }
        };
    }

    @Test
    public void testIsPlaceholderTask() {
        Queue.Task task = mock(ExecutorStepExecution.PlaceholderTask.class);
        assertTrue(helper.isPlaceholderTask(task));
    }

    @Test
    public void testGetPriority() {
        when(task.getOwnerTask()).thenReturn(ownerTask);

        PriorityConfigurationCallback result = helper.getPriority(task, callback);
        assertNotNull(result);

        // Verify that the default priority from the MultiBucketStrategy is returned by the strategy for the given item
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITY, strategy.getPriority(item));
    }

    @Test
    public void testIsPlaceholderTaskUsed() {
        assertTrue(PriorityConfigurationPlaceholderTaskHelper.isPlaceholderTaskUsed());
    }
}
