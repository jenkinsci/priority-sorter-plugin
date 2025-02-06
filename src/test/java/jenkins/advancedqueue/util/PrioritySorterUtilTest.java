package jenkins.advancedqueue.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.util.ListBoxModel;
import org.junit.jupiter.api.Test;

class PrioritySorterUtilTest {

    @Test
    void assertPriorityItemsHasExpectedSize() {
        ListBoxModel testList = PrioritySorterUtil.fillPriorityItems(3);
        assertEquals(3, testList.size());
    }

    @Test
    void assertEmptyListWhenToParameterIsZero() {
        ListBoxModel testList = PrioritySorterUtil.fillPriorityItems(0);
        assertTrue(testList.isEmpty());
    }
}
