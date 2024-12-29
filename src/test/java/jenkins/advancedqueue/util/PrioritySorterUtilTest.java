package jenkins.advancedqueue.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.util.ListBoxModel;
import org.junit.Test;

public class PrioritySorterUtilTest {

    @Test
    public void assertPriorityItemsHasExpectedSize() {
        ListBoxModel testList = PrioritySorterUtil.fillPriorityItems(3);
        assertEquals(3, testList.size());
    }

    @Test
    public void assertEmptyListWhenToParameterIsZero() {
        ListBoxModel testList = PrioritySorterUtil.fillPriorityItems(0);
        assertTrue(testList.isEmpty());
    }
}
