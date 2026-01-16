package jenkins.advancedqueue.sorter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Queue;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FQBaseStrategyTest {

    // Test implementation of FQBaseStrategy
    private static class TestFQBaseStrategy extends FQBaseStrategy {
        @Override
        float getStepSize(int priority) {
            return FQBaseStrategy.MIN_STEP_SIZE * priority;
        }

        public TestFQBaseStrategy() {
            super();
        }

        public TestFQBaseStrategy(int numberOfPriorities, int defaultPriority) {
            super(numberOfPriorities, defaultPriority);
        }
    }

    private TestFQBaseStrategy strategy;
    private Queue.Item item;
    private SorterStrategyCallback callback;
    private Queue.LeftItem leftItem;

    @BeforeEach
    void setUp() {
        strategy = new TestFQBaseStrategy();
        item = mock(Queue.Item.class);
        callback = mock(SorterStrategyCallback.class);
        leftItem = mock(Queue.LeftItem.class);
        // Clear static state before each test
        FQBaseStrategy.prio2weight.clear();
        FQBaseStrategy.maxStartedWeight = 1.0F;
    }

    @AfterEach
    void tearDown() {
        // Clean up static state after each test
        FQBaseStrategy.prio2weight.clear();
        FQBaseStrategy.maxStartedWeight = 1.0F;
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITIES_NUMBER, strategy.getNumberOfPriorities());
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITY, strategy.getDefaultPriority());
    }

    @Test
    void testCustomConstructor() {
        strategy = new TestFQBaseStrategy(10, 5);
        assertEquals(10, strategy.getNumberOfPriorities());
        assertEquals(5, strategy.getDefaultPriority());
    }

    @Test
    void testOnStartedItem() {
        // Initial maxStartedWeight should be 1.0
        float initialMaxWeight = 1.0F;

        // Set a weight less than initialMaxWeight
        strategy.onStartedItem(leftItem, initialMaxWeight - 0.5F);
        // maxStartedWeight should remain unchanged
        // Set a weight greater than initialMaxWeight
        float newWeight = initialMaxWeight + 1.0F;
        strategy.onStartedItem(leftItem, newWeight);
        // maxStartedWeight should now be newWeight
        // Verify by getting a minimum weight for a new priority
        // This should return at least the new maxStartedWeight
        float minWeight = strategy.getMinimumWeightToAssign(999);
        assertTrue(minWeight >= newWeight);
    }

    @Test
    void testOnNewItem() {
        int priority = 2;
        float startWeight = 1.0F;
        float expectedStepSize = FQBaseStrategy.MIN_STEP_SIZE * priority;
        float expectedWeight = startWeight * (1.0F + expectedStepSize);

        when(callback.getPriority()).thenReturn(priority);
        when(callback.setWeightSelection(expectedWeight)).thenReturn(callback);

        // Store a value in the prio2weight map
        FQBaseStrategy.prio2weight.put(priority, startWeight);

        SorterStrategyCallback result = strategy.onNewItem(item, callback);

        assertEquals(callback, result);
    }

    @Test
    void testGetMinimumWeightToAssign() {
        int priority = 3;
        float weight = 2.5F;

        // Initially should return default (1.0F) when no value is in the map
        assertEquals(1.0F, strategy.getMinimumWeightToAssign(999));

        // Store a value directly in the map
        FQBaseStrategy.prio2weight.put(priority, weight);

        // Now the minimum weight should be at least the stored weight
        float minWeight = strategy.getMinimumWeightToAssign(priority);
        assertTrue(minWeight >= weight, "Expected minimum weight " + minWeight + " to be >= " + weight);
    }

    @Test
    void testGetWeightToUse() {
        int priority = 2;
        float minimumWeight = 1.5F;
        float expectedStepSize = FQBaseStrategy.MIN_STEP_SIZE * priority;
        float expectedWeight = minimumWeight * (1.0F + expectedStepSize);

        float calculatedWeight = strategy.getWeightToUse(priority, minimumWeight);

        assertEquals(expectedWeight, calculatedWeight);
    }

    @Test
    void testGetWeightToUseWithInfinity() {
        // Test the case where the calculation would result in a positive infinity
        int priority = 1;
        float minimumWeight = Float.MAX_VALUE;

        float calculatedWeight = strategy.getWeightToUse(priority, minimumWeight);

        // Should reset and return the minimum weight
        assertEquals(1.0F, calculatedWeight);
    }
}
