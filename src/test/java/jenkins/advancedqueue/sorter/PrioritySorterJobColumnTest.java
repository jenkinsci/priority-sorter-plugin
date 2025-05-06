package jenkins.advancedqueue.sorter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.util.ArrayList;
import java.util.Calendar;
import jenkins.advancedqueue.PrioritySorterJobColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PrioritySorterJobColumnTest {

    private static JenkinsRule j;

    private static FreeStyleProject project;
    private static ItemInfo itemInfo;
    private static PrioritySorterJobColumn column;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        project = j.createFreeStyleProject("test-job");
        Queue.WaitingItem waitingItem = new Queue.WaitingItem(Calendar.getInstance(), project, new ArrayList<>());
        itemInfo = new ItemInfo(waitingItem);
        column = new PrioritySorterJobColumn();
    }

    @Test
    void getPriorityReturnsPendingWhenItemInfoIsNull() throws Exception {
        assertEquals("Pending", column.getPriority(project));
    }

    @Test
    void getPriorityReturnsCorrectPriority() throws Exception {
        QueueItemCache.get().addItem(itemInfo);
        assertEquals("0", column.getPriority(project));
    }

    @Test
    void getPriorityReturnsPendingForNonExistentJob() throws Exception {
        assertEquals("Pending", column.getPriority(project));
    }

    @Test
    void descriptorImplDisplayNameIsCorrect() {
        assertEquals("Priority Value", column.getDescriptor().getDisplayName());
    }

    @Test
    void descriptorImplShownByDefaultIsFalse() {
        PrioritySorterJobColumn.DescriptorImpl descriptor =
                (PrioritySorterJobColumn.DescriptorImpl) column.getDescriptor();
        assertFalse(descriptor.shownByDefault());
    }
}
