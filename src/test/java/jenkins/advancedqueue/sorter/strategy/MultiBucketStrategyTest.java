package jenkins.advancedqueue.sorter.strategy;

import static org.junit.Assert.assertEquals;

import hudson.model.Queue;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MultiBucketStrategyTest extends MultiBucketStrategy {

    /**
     * @param item           the {@link hudson.model.Queue.WaitingItem} or {@link hudson.model.BuildableItem} that
     *                       enters the queue
     * @param weightCallback the callback holds the priority to use anded the called method must set
     *                       the weight before returning
     * @return
     */
    @Override
    public SorterStrategyCallback onNewItem(@NotNull Queue.Item item, SorterStrategyCallback weightCallback) {
        return weightCallback;
    }

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static MultiBucketStrategy strategy;
    private static MultiBucketStrategy.MultiBucketStrategyDescriptor descriptor;
    private static final int DEFAULT_PRIORITIES_NUMBER = 10;
    private static final int DEFAULT_PRIORITY = 5;

    @Before
    public void setUp() {
        strategy = new MultiBucketStrategy(DEFAULT_PRIORITIES_NUMBER, DEFAULT_PRIORITY) {
            @Override
            public SorterStrategyCallback onNewItem(@NotNull Queue.Item item, SorterStrategyCallback weightCallback) {
                return weightCallback;
            }
        };

        descriptor = new MultiBucketStrategy.MultiBucketStrategyDescriptor() {
            @Override
            public String getShortName() {
                return "";
            }

            @Override
            protected MultiBucketStrategy getStrategy() {
                return strategy;
            }
        };
    }

    @Test
    public void getNumberOfPrioritiesReturnsCorrectValue() {
        assertEquals(10, strategy.getNumberOfPriorities());
    }

    @Test
    public void getDefaultPriorityReturnsCorrectValue() {
        assertEquals(5, strategy.getDefaultPriority());
    }

    @Test
    public void doFillDefaultPriorityItemsReturnsCorrectItems() {
        ListBoxModel items = descriptor.doFillDefaultPriorityItems();
        assertEquals(10, items.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(String.valueOf(i + 1), items.get(i).value);
        }
    }

    @Test
    public void doUpdateDefaultPriorityItemsHandlesInvalidInput() {
        ListBoxModel items = descriptor.doUpdateDefaultPriorityItems("invalid");
        assertEquals(3, items.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(String.valueOf(i + 1), items.get(i).value);
        }
    }

    @Test
    public void doUpdateDefaultPriorityItemsHandlesValidInput() {
        ListBoxModel items = descriptor.doUpdateDefaultPriorityItems("3");
        assertEquals(3, items.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(String.valueOf(i + 1), items.get(i).value);
        }
    }

    @Test
    public void doUpdateDefaultPriorityItemsHandlesNegativeInput() {
        ListBoxModel items = descriptor.doUpdateDefaultPriorityItems("-1");
        assertEquals(0, items.size());
    }

    @Test
    public void doUpdateDefaultPriorityItemsHandlesZeroInput() {
        ListBoxModel items = descriptor.doUpdateDefaultPriorityItems("0");
        assertEquals(0, items.size());
    }

    @Test
    public void doUpdateDefaultPriorityItemsHandlesLargeInput() {
        ListBoxModel items = descriptor.doUpdateDefaultPriorityItems("100");
        assertEquals(100, items.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(String.valueOf(i + 1), items.get(i).value);
        }
    }
}
