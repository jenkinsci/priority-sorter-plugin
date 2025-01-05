package jenkins.advancedqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PrioritySorterJobColumnTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject project;
    private static Calendar calendar;
    private static Queue.WaitingItem waitingItem;
    private static ItemInfo itemInfo;
    private static PrioritySorterJobColumn column;

    @BeforeClass
    public static void setUp() throws IOException {
        project = j.createFreeStyleProject("test-job");
        calendar = Calendar.getInstance();
        waitingItem = new Queue.WaitingItem(calendar, project, new ArrayList<>());
        itemInfo = new ItemInfo(waitingItem);
        column = new PrioritySorterJobColumn();
    }

    @Test
    public void getPriorityReturnsPendingWhenItemInfoIsNull() throws Exception {

        PrioritySorterJobColumn column = new PrioritySorterJobColumn();
        assertEquals("Pending", column.getPriority(project));
    }

    @Test
    public void getPriorityReturnsCorrectPriority() throws Exception {

        QueueItemCache.get().addItem(itemInfo);

        assertEquals("0", column.getPriority(project));
    }

    @Test
    public void getPriorityReturnsPendingForNonExistentJob() throws Exception {

        PrioritySorterJobColumn column = new PrioritySorterJobColumn();
        assertEquals("Pending", column.getPriority(project));
    }

    @Test
    public void descriptorImplDisplayNameIsCorrect() {

        PrioritySorterJobColumn.DescriptorImpl descriptor = new PrioritySorterJobColumn.DescriptorImpl();
        assertEquals("Priority Value", descriptor.getDisplayName());
    }

    @Test
    public void descriptorImplShownByDefaultIsFalse() {
        PrioritySorterJobColumn.DescriptorImpl descriptor = new PrioritySorterJobColumn.DescriptorImpl();
        assertFalse(descriptor.shownByDefault());
    }
}
