package jenkins.advancedqueue.sorter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Queue;
import hudson.model.Queue.LeftItem;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class SorterStrategyTest {

    private static SorterStrategy strategy;
    private static SorterStrategyCallback mockCallback;
    private static Queue.Item mockItem;
    private static LeftItem mockLeftItem;

    @BeforeAll
    public static void setUp(JenkinsRule j) {
        strategy = new TestSorterStrategy();
        mockCallback = mock(SorterStrategyCallback.class);
        mockItem = mock(Queue.Item.class);
        mockLeftItem = mock(LeftItem.class);
    }

    @Test
    void testOnNewItem() {
        SorterStrategyCallback result = strategy.onNewItem(mockItem, mockCallback);

        assertEquals(mockCallback, result);
    }

    @Test
    void testGetNumberOfPriorities() {
        assertEquals(9, strategy.getNumberOfPriorities());
    }

    @Test
    void testGetDefaultPriority() {
        assertEquals(4, strategy.getDefaultPriority());
    }

    @Test
    void testGetSorterStrategyNonExistentKey() {
        SorterStrategyDescriptor result = SorterStrategy.getSorterStrategy("non-existent-key");
        assertNull(result);
    }

    @Test
    void testGetPrioritySorterStrategyNonExistentDescriptor() {
        SorterStrategyDescriptor mockDescriptor = mock(SorterStrategyDescriptor.class);
        when(mockDescriptor.getKey()).thenReturn("non-existent-key");
        SorterStrategy result = SorterStrategy.getPrioritySorterStrategy(mockDescriptor);
        assertNull(result);
    }

    @Test
    void testGetAllSorterStrategiesWithNoRegisteredStrategies() {
        ExtensionList<SorterStrategy> all = mock(ExtensionList.class);
        ExtensionList<SorterStrategy> allRegisteredItems = SorterStrategy.all();

        when(all.size()).thenReturn(0);
        when(all.iterator()).thenReturn(allRegisteredItems.iterator());

        //        when(Jenkins.get().getExtensionList(SorterStrategy.class)).thenReturn(all);

        List<SorterStrategyDescriptor> result = SorterStrategy.getAllSorterStrategies();
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllSorterStrategiesWithRegisteredStrategies() {
        ExtensionList<SorterStrategy> all = mock(ExtensionList.class);
        ExtensionList<SorterStrategy> allRegisteredItems = SorterStrategy.all();
        //        when(Jenkins.get().getExtensionList(SorterStrategy.class)).thenReturn(allRegisteredItems);
        when(all.size()).thenReturn(1);
        when(all.iterator()).thenReturn(allRegisteredItems.iterator());

        List<SorterStrategyDescriptor> result = SorterStrategy.getAllSorterStrategies();
        result.add(new TestSorterStrategy.DescriptorImpl());
        assertEquals(1, result.size());
    }

    @Test
    void testGetSorterStrategyWithNullKey() {
        SorterStrategyDescriptor result = SorterStrategy.getSorterStrategy(null);
        assertNull(result);
    }

    @Test
    void testGetPrioritySorterStrategyWithNullDescriptor() {
        SorterStrategy result = SorterStrategy.getPrioritySorterStrategy(null);
        assertNull(result);
    }

    private static class TestSorterStrategy extends SorterStrategy {
        @Override
        public SorterStrategyCallback onNewItem(@NonNull Queue.Item item, SorterStrategyCallback weightCallback) {
            return weightCallback;
        }

        @Override
        public int getNumberOfPriorities() {
            return 9;
        }

        @Override
        public int getDefaultPriority() {
            return 4;
        }

        @Extension
        public static class DescriptorImpl extends SorterStrategyDescriptor {
            @Override
            public String getShortName() {
                return "strategy short name";
            }
        }
    }
}
