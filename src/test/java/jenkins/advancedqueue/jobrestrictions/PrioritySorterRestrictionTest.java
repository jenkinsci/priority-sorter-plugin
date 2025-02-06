package jenkins.advancedqueue.jobrestrictions;

import static hudson.model.Queue.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;
import java.lang.reflect.Field;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

class PrioritySorterRestrictionTest {

    private PrioritySorterRestriction restriction;
    private BuildableItem mockedBuildableItem;
    private static final long MOCKED_BUILDABLE_ITEM_ID = 1L;
    private static final int LOWER_PRIORITY = 1;
    private static final int UPPER_PRIORITY = 1;

    @BeforeEach
    void beforeEach() {
        this.restriction = new PrioritySorterRestriction(LOWER_PRIORITY, UPPER_PRIORITY);
        this.mockedBuildableItem = mock(BuildableItem.class);
        when(mockedBuildableItem.getId()).thenReturn(MOCKED_BUILDABLE_ITEM_ID);
    }

    @Test
    void testCanTake_MissingItemInfo() throws NoSuchFieldException, IllegalAccessException {
        Task mockedTask = mock(Task.class);
        setTaskInMockedBuildableItem(mockedBuildableItem, mockedTask);

        try (MockedStatic<QueueItemCache> mockedQueueItemCache = mockStatic(QueueItemCache.class)) {
            QueueItemCache mockedItemCache = mock(QueueItemCache.class);
            mockedQueueItemCache.when(QueueItemCache::get).thenReturn(mockedItemCache);
            when(mockedItemCache.getItem(MOCKED_BUILDABLE_ITEM_ID)).thenReturn(null);
            assertTrue(restriction.canTake(mockedBuildableItem));
        }
    }

    private void setTaskInMockedBuildableItem(BuildableItem buildableItem, Task task)
            throws NoSuchFieldException, IllegalAccessException {
        Field taskField = BuildableItem.class.getField("task");
        taskField.setAccessible(true);
        taskField.set(buildableItem, task);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 6})
    void testCanTake_PriorityRange(int priority) {
        ItemInfo mockItemInfo = mock(ItemInfo.class);
        when(mockItemInfo.getPriority()).thenReturn(priority);

        try (MockedStatic<QueueItemCache> mockedCache = mockStatic(QueueItemCache.class)) {
            QueueItemCache mockCache = mock(QueueItemCache.class);
            mockedCache.when(QueueItemCache::get).thenReturn(mockCache);
            when(mockCache.getItem(MOCKED_BUILDABLE_ITEM_ID)).thenReturn(mockItemInfo);
            if (priority >= LOWER_PRIORITY && priority <= UPPER_PRIORITY)
                assertTrue(
                        restriction.canTake(mockedBuildableItem),
                        "Should allow execution when priority is within range.");
            else
                assertFalse(
                        restriction.canTake(mockedBuildableItem),
                        "Should not allow execution when priority is outside range.");
        }
    }

    @Test
    void testCanTake() {
        Run mockedRun = mock(Run.class);
        assertTrue(restriction.canTake(mockedRun), "canTake should return true when passed a Run object.");
    }

    @Test
    void testGetFromPriority() {
        assertEquals(LOWER_PRIORITY, restriction.getFromPriority(), "From Priority should be " + LOWER_PRIORITY);
    }

    @Test
    void testGetToPriority() {
        assertEquals(UPPER_PRIORITY, restriction.getToPriority(), "To Priority should be " + UPPER_PRIORITY);
    }
}
