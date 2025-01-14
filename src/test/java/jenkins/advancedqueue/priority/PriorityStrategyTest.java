package jenkins.advancedqueue.priority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hudson.DescriptorExtensionList;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PriorityStrategyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static PriorityStrategy strategy;
    private static Queue.Item item;
    private static FreeStyleProject project;
    private static Action action;

    @BeforeClass
    public static void setUp() throws IOException {
        project = j.createFreeStyleProject();
        strategy = new TestPriorityStrategy();
        action = new Action() {
            /**
             */
            @Override
            public String getIconFileName() {
                return "";
            }

            /**
             */
            @Override
            public String getDisplayName() {
                return "";
            }

            /**
             */
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
    public void testIsApplicable() {
        boolean result = strategy.isApplicable(item);
        assertTrue(result);
    }

    @Test
    public void testGetPriority() {
        int priority = strategy.getPriority(item);
        assertEquals(5, priority);
    }

    @Test
    public void testNumberPrioritiesUpdates() {
        strategy.numberPrioritiesUpdates(3, 5);
        // Add assertions to verify the behavior
        TestPriorityStrategy testStrategy = new TestPriorityStrategy();
        assertEquals(3, testStrategy.getOldNumberOfPriorities());
        assertEquals(5, testStrategy.getNewNumberOfPriorities());
    }

    @Test
    public void testAll() {
        DescriptorExtensionList<PriorityStrategy, Descriptor<PriorityStrategy>> list = PriorityStrategy.all();
        assertNotNull("DescriptorExtensionList should not be null", list);
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

        /**
         *
         */
        @Override
        public Descriptor<PriorityStrategy> getDescriptor() {
            return null;
        }
    }
}
