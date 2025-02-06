package jenkins.advancedqueue.sorter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WFQStrategyTest {

    @Test
    void testStepSize() {
        assertEquals(1 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(1), 0F);
        assertEquals(2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(2), 0F);
        assertEquals(3 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(3), 0F);
        assertEquals(4 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(4), 0F);
    }

    @Test
    void testGetWeightToUse() {
        assertEquals(1.00000F + FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(1, 1.00000F), 0F);
        assertEquals(1.00001F + FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(1, 1.00001F), 0F);
        assertEquals(1.00000F + 2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(2, 1.00000F), 0F);
        assertEquals(1.00001F + 2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(2, 1.00001F), 0F);
    }
}
