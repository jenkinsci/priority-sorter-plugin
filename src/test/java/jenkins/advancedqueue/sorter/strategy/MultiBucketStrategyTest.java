package jenkins.advancedqueue.sorter.strategy;

import static org.junit.Assert.assertEquals;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Queue;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MultiBucketStrategyTest extends MultiBucketStrategy {

    /** @{inheritDoc} */
    @Override
    public SorterStrategyCallback onNewItem(@NonNull Queue.Item item, SorterStrategyCallback weightCallback) {
        return weightCallback;
    }

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static MultiBucketStrategy strategy;
    private static MultiBucketStrategy.MultiBucketStrategyDescriptor descriptor;
    private static final int TEST_PRIORITY_COUNT = 10;
    private static final int TEST_PRIORITY_DEFAULT = 5;

    @Before
    public void setUp() {
        strategy = new MultiBucketStrategy(TEST_PRIORITY_COUNT, TEST_PRIORITY_DEFAULT) {
            @Override
            public SorterStrategyCallback onNewItem(@NonNull Queue.Item item, SorterStrategyCallback weightCallback) {
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
        assertEquals(TEST_PRIORITY_COUNT, strategy.getNumberOfPriorities());
    }

    @Test
    public void getDefaultPriorityReturnsCorrectValue() {
        assertEquals(TEST_PRIORITY_DEFAULT, strategy.getDefaultPriority());
    }

    @Test
    public void doFillDefaultPriorityItemsReturnsCorrectItems() {
        ListBoxModel items = descriptor.doFillDefaultPriorityItems();
        assertEquals(TEST_PRIORITY_COUNT, items.size());
        for (int i = 0; i < TEST_PRIORITY_COUNT; i++) {
            assertEquals(String.valueOf(i + 1), items.get(i).value);
        }
    }

    @Test
    public void doUpdateDefaultPriorityItemsHandlesInvalidInput() {
        ListBoxModel items = descriptor.doUpdateDefaultPriorityItems("invalid");
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITY, items.size());
        for (int i = 0; i < MultiBucketStrategy.DEFAULT_PRIORITY; i++) {
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
