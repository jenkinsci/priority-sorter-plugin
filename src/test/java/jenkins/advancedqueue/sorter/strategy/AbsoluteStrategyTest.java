package jenkins.advancedqueue.sorter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Queue;
import jenkins.advancedqueue.sorter.SorterStrategyCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbsoluteStrategyTest {

    private AbsoluteStrategy strategy;
    private Queue.Item item;
    private SorterStrategyCallback callback;

    @BeforeEach
    void setUp() {
        strategy = new AbsoluteStrategy();
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
        strategy = new AbsoluteStrategy(10, 5);
        assertEquals(10, strategy.getNumberOfPriorities());
        assertEquals(5, strategy.getDefaultPriority());
    }

    @Test
    void testOnNewItem() {
        // Test with different priorities
        for (int priority = 1; priority <= 5; priority++) {
            when(callback.getPriority()).thenReturn(priority);
            when(callback.setWeightSelection(priority)).thenReturn(callback);

            SorterStrategyCallback result = strategy.onNewItem(item, callback);

            assertEquals(callback, result, "Callback should be returned unchanged");
        }
    }

    @Test
    void testDescriptorImplementation() {
        AbsoluteStrategy.DescriptorImpl descriptor = new AbsoluteStrategy.DescriptorImpl();

        // Test that display name and short name are not null or empty
        String displayName = descriptor.getDisplayName();
        String shortName = descriptor.getShortName();

        assertEquals(false, displayName == null || displayName.isEmpty(), "Display name should not be null or empty");
        assertEquals(false, shortName == null || shortName.isEmpty(), "Short name should not be null or empty");
    }
}
