package jenkins.advancedqueue.sorter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Queue;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WFQStrategyTest {

    private WFQStrategy strategy;
    private Queue.Item item;
    private SorterStrategyCallback callback;

    @BeforeEach
    void setUp() {
        strategy = new WFQStrategy();
        item = mock(Queue.Item.class);
        callback = mock(SorterStrategyCallback.class);
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITIES_NUMBER, strategy.getNumberOfPriorities());
        assertEquals(MultiBucketStrategy.DEFAULT_PRIORITY, strategy.getDefaultPriority());
    }

    @Test
    void testCustomConstructor() {
        strategy = new WFQStrategy(10, 5);
        assertEquals(10, strategy.getNumberOfPriorities());
        assertEquals(5, strategy.getDefaultPriority());
    }

    @Test
    void testStepSize() {
        // The step size should be priority * MIN_STEP_SIZE
        assertEquals(1 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(1), 0F);
        assertEquals(2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(2), 0F);
        assertEquals(3 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(3), 0F);
        assertEquals(4 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(4), 0F);
    }

    @Test
    void testGetWeightToUse() {
        assertEquals(1.00000F + 1 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(1, 1.00000F), 0F);
        assertEquals(1.00001F + 1 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(1, 1.00001F), 0F);
        assertEquals(1.00000F + 2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(2, 1.00000F), 0F);
        assertEquals(1.00001F + 2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(2, 1.00001F), 0F);
    }

    @Test
    void testOnNewItem() {
        int priority = 2;
        float expectedStepSize = FQBaseStrategy.MIN_STEP_SIZE * priority;
        float startWeight = 1.0F;
        float expectedWeight = startWeight * (1.0F + expectedStepSize);

        when(callback.getPriority()).thenReturn(priority);
        when(callback.setWeightSelection(expectedWeight)).thenReturn(callback);

        // Add to prio2weight map to ensure getMinimumWeightToAssign returns the
        // expected value
        FQBaseStrategy.prio2weight.put(priority, startWeight);

        SorterStrategyCallback result = strategy.onNewItem(item, callback);

        assertEquals(callback, result, "Callback should be returned unchanged");
    }

    @Test
    void testInfinityHandling() {
        // Test the special case when weight would overflow to infinity
        float weight = Float.MAX_VALUE;

        // This should return 1.0 instead of infinity
        assertEquals(1.0F, strategy.getWeightToUse(1, weight), 0.0001F);
    }

    @Test
    void testGetMinimumWeightToAssign() {
        int priority = 3;
        float weight = 2.5F;

        // Store a weight in the map
        FQBaseStrategy.prio2weight.put(priority, weight);

        // Now the minimum weight should be the stored weight
        float minWeight = strategy.getMinimumWeightToAssign(priority);
        assertTrue(minWeight >= weight, "Expected minimum weight " + minWeight + " to be >= " + weight);
    }

    @Test
    void testDescriptorImplementation() {
        WFQStrategy.DescriptorImpl descriptor = new WFQStrategy.DescriptorImpl();

        // Test that display name and short name are not null or empty
        String displayName = descriptor.getDisplayName();
        String shortName = descriptor.getShortName();

        assertNotNull(displayName);
        assertFalse(displayName.isEmpty());
        assertNotNull(shortName);
        assertFalse(shortName.isEmpty());
    }
}
