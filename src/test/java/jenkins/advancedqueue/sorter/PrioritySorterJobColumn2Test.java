package jenkins.advancedqueue.sorter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.util.ArrayList;
import java.util.Calendar;
import jenkins.advancedqueue.PrioritySorterJobColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Surefire 3.6.0 changed the order of execution of unit tests.  That
 * exposed an order dependency in the tests, so these tests were split
 * to a separate source file.  That was simpler than identifying the
 * cause of the order dependency in the tests.
 */
@WithJenkins
class PrioritySorterJobColumn2Test {

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
    void getPriorityReturnsPendingForNonExistentJob() throws Exception {
        assertEquals("Pending", column.getPriority(project));
    }
}
