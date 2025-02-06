package jenkins.advancedqueue.sorter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FQStrategyTest {

    @Test
    void testStepSize() {
        assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(1), 0F);
        assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(2), 0F);
        assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(3), 0F);
        assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(4), 0F);
    }

    @Test
    void testGetWeightToUse() {
        assertEquals(1.00000F + FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getWeightToUse(1, 1.00000F), 0F);
        assertEquals(1.00001F + FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getWeightToUse(1, 1.00001F), 0F);
        assertEquals(1F, new FQStrategy().getWeightToUse(1, Float.MAX_VALUE), 0F);
        assertIncreasingWeight(1F);
        assertIncreasingWeight(100000F);
    }

    private void assertIncreasingWeight(float initialWeight) {
        float previousWeight = initialWeight;
        for (int i = 0; i < 10; ++i) {
            float newWeight = new FQStrategy().getWeightToUse(1, previousWeight);
            assertTrue(
                    newWeight > previousWeight,
                    "New weight %s should be greater than previous weight %s".formatted(newWeight, previousWeight));
            previousWeight = newWeight;
        }
    }
}
