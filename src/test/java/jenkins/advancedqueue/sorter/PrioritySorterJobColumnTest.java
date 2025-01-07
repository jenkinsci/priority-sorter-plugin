package jenkins.advancedqueue.sorter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import jenkins.advancedqueue.PrioritySorterJobColumn;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PrioritySorterJobColumnTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject project;
    private static ItemInfo itemInfo;
    private static PrioritySorterJobColumn column;

    @BeforeClass
    public static void setUp() throws IOException {
        project = j.createFreeStyleProject("test-job");
        Queue.WaitingItem waitingItem = new Queue.WaitingItem(Calendar.getInstance(), project, new ArrayList<>());
        itemInfo = new ItemInfo(waitingItem);
        column = new PrioritySorterJobColumn();
    }

    @Test
    public void getPriorityReturnsPendingWhenItemInfoIsNull() throws Exception {
        assertEquals("Pending", column.getPriority(project));
    }

    @Test
    public void getPriorityReturnsCorrectPriority() throws Exception {
        QueueItemCache.get().addItem(itemInfo);
        assertEquals("0", column.getPriority(project));
    }

    @Test
    public void getPriorityReturnsPendingForNonExistentJob() throws Exception {
        assertEquals("Pending", column.getPriority(project));
    }

    @Test
    public void descriptorImplDisplayNameIsCorrect() {
        assertEquals("Priority Value", column.getDescriptor().getDisplayName());
    }

    @Test
    public void descriptorImplShownByDefaultIsFalse() {
        PrioritySorterJobColumn.DescriptorImpl descriptor =
                (PrioritySorterJobColumn.DescriptorImpl) column.getDescriptor();
        assertFalse(descriptor.shownByDefault());
    }
}
