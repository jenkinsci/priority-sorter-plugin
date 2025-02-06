package jenkins.advancedqueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PriorityCalculationsUtilTest {

    @Test
    void testScale() {
        assertEquals(10, PriorityCalculationsUtil.scale(100, 10, 100));
        assertEquals(1, PriorityCalculationsUtil.scale(100, 10, 9));
        assertEquals(5, PriorityCalculationsUtil.scale(100, 10, 50));
        assertEquals(8, PriorityCalculationsUtil.scale(100, 10, 75));

        assertEquals(1, PriorityCalculationsUtil.scale(5, 10, 1));
        assertEquals(3, PriorityCalculationsUtil.scale(5, 10, 2));
        assertEquals(5, PriorityCalculationsUtil.scale(5, 10, 3));
        assertEquals(8, PriorityCalculationsUtil.scale(5, 10, 4));
        assertEquals(10, PriorityCalculationsUtil.scale(5, 10, 5));
    }

    @Test
    void testScaleUseDefaultPriority() {
        assertEquals(
                PriorityCalculationsUtil.getUseDefaultPriorityPriority(),
                PriorityCalculationsUtil.scale(5, 10, PriorityCalculationsUtil.getUseDefaultPriorityPriority()));
    }
}
