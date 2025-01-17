package jenkins.advancedqueue.sorter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Queue.Task;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.FutureImpl;
import hudson.model.queue.SubTask;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ItemInfoTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static Item item;
    private static Task task;
    private static ItemInfo itemInfo;
    private static List<Action> actions;
    private static Queue q;

    @BeforeClass
    public static void setUp() throws IOException {
        q = Jenkins.get().getQueue();

        task = new Queue.Task() {
            @Override
            public String getDisplayName() {
                return "";
            }

            @Override
            public String getName() {
                return "Test Task";
            }

            @Override
            public String getFullDisplayName() {
                return "Test Task Full Display Name";
            }

            @Override
            public void checkAbortPermission() {}

            @Override
            public boolean hasAbortPermission() {
                return true;
            }

            @Override
            public String getUrl() {
                return "test/task/url";
            }

            @Override
            public Collection<? extends SubTask> getSubTasks() {
                return Collections.singletonList(this);
            }

            @Override
            public Queue.Executable createExecutable() throws IOException {
                return null;
            }

            @Override
            public CauseOfBlockage getCauseOfBlockage() {
                return null;
            }
        };

        actions = Collections.emptyList();

        //        item = new Queue.Item(task, actions, 0L, null) {

        Task mockTask = mock(Task.class);
        List<Action> actions = Collections.emptyList();
        FutureImpl future = mock(FutureImpl.class);

        //            ConcreteQueueItem customItem = new ConcreteQueueItem(mockTask, actions, 12345L, future) {
        item = new Item(mockTask, actions, 12345L, future) {

            @Override
            public void enter(Queue q) {
                System.out.println("Entered queue: " + q);
            }

            @Override
            public boolean leave(Queue q) {
                System.out.println("Leaving queue: " + q);
                return true;
            }
        };

        itemInfo = new ItemInfo(item);
    }

    @Test
    public void constructorInitializesFieldsCorrectly() {
        assertEquals(item.getId(), itemInfo.getItemId());
        assertEquals(item.getInQueueSince(), itemInfo.getInQueueSince());
        assertEquals(item.task.getName(), itemInfo.getJobName());
        assertEquals(ItemStatus.WAITING, itemInfo.getItemStatus());
    }

    @Test
    public void setPrioritySelectionSetsFieldsCorrectly() {
        itemInfo.setPrioritySelection(5, 1, null);
        assertEquals(5, itemInfo.getPriority());
        assertEquals(1, itemInfo.getJobGroupId());
        assertNull(itemInfo.getPriorityStrategy());
    }

    @Test
    public void setWeightSelectionSetsWeightCorrectly() {
        itemInfo.setWeightSelection(10.5f);
        assertEquals(10.5f, itemInfo.getWeight(), 0.0f);
    }

    @Test
    public void setBuildableSetsStatusCorrectly() {
        itemInfo.setBuildable();
        assertEquals(ItemStatus.BUILDABLE, itemInfo.getItemStatus());
    }

    @Test
    public void setBlockedSetsStatusCorrectly() {
        itemInfo.setBlocked();
        assertEquals(ItemStatus.BLOCKED, itemInfo.getItemStatus());
    }

    @Test
    public void compareToComparesCorrectly() {
        ItemInfo other = new ItemInfo(item);
        other.setWeightSelection(10.0f);
        itemInfo.setWeightSelection(5.0f);
        assertTrue(itemInfo.compareTo(other) < 0);
    }

    @Test
    public void equalsAndHashCodeWorkCorrectly() {
        ItemInfo other = new ItemInfo(item);
        assertEquals(itemInfo, other);
        assertEquals(itemInfo.hashCode(), other.hashCode());
    }

    @Test
    public void toStringReturnsCorrectFormat() {
        String expected = String.format(
                "Id: %s, JobName: %s, jobGroupId: %s, reason: %s, priority: %s, weight: %s, status: %s",
                itemInfo.getItemId(),
                itemInfo.getJobName(),
                itemInfo.getJobGroupId(),
                "<none>",
                itemInfo.getPriority(),
                itemInfo.getWeight(),
                itemInfo.getItemStatus());
        assertEquals(expected, itemInfo.toString());
    }

    @Test
    public void getDecisionLogReturnsCorrectLog() {
        itemInfo.addDecisionLog(1, "Test log");
        String expected = "  Test log\n";
        assertEquals(expected, itemInfo.getDescisionLog());
    }
}
