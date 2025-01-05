package jenkins.advancedqueue.sorter;

import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.SubTask;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ItemInfoTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    private Queue.Item item;
    private ItemInfo itemInfo;

    @Before
    public void setUp() throws IOException {
        Queue.Task task = new Queue.Task() {
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
            public void checkAbortPermission() {
            }

            @Override
            public boolean hasAbortPermission() {
                return true;
            }

            @Override
            public String getUrl() {
                return "test/task/url";
            }

            @Override
            public boolean isConcurrentBuild() {
                return false;
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

        item = new Queue.Item(task, Collections.emptyList(), 0L, null) {
            private Queue q;

            @Override
            public CauseOfBlockage getCauseOfBlockage() {
                System.out.println("Cause of blockage" + this);
                return null;
            }

            @Override
            public void enter(Queue q) {
                this.q = q;
                // Implementation for enter method
                System.out.println("Item entered the queue");
            }

            @Override
            public boolean leave(Queue q) {
                // Implementation for leave method
                System.out.println("Item left the queue");
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
                itemInfo.getItemId(), itemInfo.getJobName(), itemInfo.getJobGroupId(), "<none>",
                itemInfo.getPriority(), itemInfo.getWeight(), itemInfo.getItemStatus());
        assertEquals(expected, itemInfo.toString());
    }

    @Test
    public void getDescisionLogReturnsCorrectLog() {
        itemInfo.addDecisionLog(1, "Test log");
        String expected = "  Test log\n";
        assertEquals(expected, itemInfo.getDescisionLog());
    }
}