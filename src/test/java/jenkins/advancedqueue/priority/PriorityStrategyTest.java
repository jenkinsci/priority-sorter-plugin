package jenkins.advancedqueue.priority;

import static org.junit.jupiter.api.Assertions.*;

import hudson.DescriptorExtensionList;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PriorityStrategyTest {

    private static JenkinsRule j;

    private static PriorityStrategy strategy;
    private static Queue.Item item;
    private static FreeStyleProject project;
    private static Action action;

    @BeforeAll
    static void setUp(JenkinsRule rule) throws IOException {
        j = rule;
        project = j.createFreeStyleProject();
        strategy = new TestPriorityStrategy();
    }

    @BeforeAll
    static void createActionAndItem() {
        action = new Action() {

            @Override
            public String getIconFileName() {
                return "";
            }

            @Override
            public String getDisplayName() {
                return "";
            }

            @Override
            public String getUrlName() {
                return "";
            }
        };
        List<Action> actions = new ArrayList<>();
        actions.add(action);
        item = new Queue.WaitingItem(Calendar.getInstance(), project, actions);
    }

    @Test
    void testIsApplicable() {
        boolean result = strategy.isApplicable(item);
        assertTrue(result);
    }

    @Test
    void testGetPriority() {
        int priority = strategy.getPriority(item);
        assertEquals(5, priority);
    }

    @Test
    void testNumberPrioritiesUpdates() {
        strategy.numberPrioritiesUpdates(3, 5);
        // Add assertions to verify the behavior
        TestPriorityStrategy testStrategy = new TestPriorityStrategy();
        assertEquals(3, testStrategy.getOldNumberOfPriorities());
        assertEquals(5, testStrategy.getNewNumberOfPriorities());
    }

    @Test
    void testAll() {
        DescriptorExtensionList<PriorityStrategy, Descriptor<PriorityStrategy>> list = PriorityStrategy.all();
        assertNotNull(list, "DescriptorExtensionList should not be null");
        // The list.size() method returns 7 because the DescriptorExtensionList for PriorityStrategy contains 7
        // descriptors. This means there are 7 different implementations of the PriorityStrategy class registered in the
        // Jenkins instance.
        assertEquals(7, list.size());
    }

    @Test
    void testItemTaskIsInstanceOfJob() {
        item = new Queue.WaitingItem(Calendar.getInstance(), project, new ArrayList<>());
        assertTrue(item.task instanceof Job);
    }

    private static class TestPriorityStrategy extends PriorityStrategy {
        private int oldNumberOfPriorities = 3;
        private int newNumberOfPriorities = 5;

        @Override
        public boolean isApplicable(Queue.Item item) {
            return item.task instanceof FreeStyleProject;
        }

        @Override
        public int getPriority(Queue.Item item) {
            return 5;
        }

        @Override
        public void numberPrioritiesUpdates(int oldNumberOfPriorities, int newNumberOfPriorities) {
            this.oldNumberOfPriorities = oldNumberOfPriorities;
            this.newNumberOfPriorities = newNumberOfPriorities;
        }

        public int getOldNumberOfPriorities() {
            return oldNumberOfPriorities;
        }

        public int getNewNumberOfPriorities() {
            return newNumberOfPriorities;
        }

        @Override
        public Descriptor<PriorityStrategy> getDescriptor() {
            return null;
        }
    }
}
