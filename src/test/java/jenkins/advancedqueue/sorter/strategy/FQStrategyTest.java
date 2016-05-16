package jenkins.advancedqueue.sorter.strategy;

import org.junit.Assert;
import org.junit.Test;

public class FQStrategyTest {

	@Test
	public void testStepSize() {
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(1), 0F);
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(2), 0F);
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(3), 0F);
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getStepSize(4), 0F);
	}

	@Test
	public void testGetWeightToUse() {
		Assert.assertEquals(1.00000F + FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getWeightToUse(1, 1.00000F), 0F);
		Assert.assertEquals(1.00001F + FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getWeightToUse(1, 1.00001F), 0F);
		assertIncreasingWeight(1F);
		assertIncreasingWeight(100000F);
	}

	private void assertIncreasingWeight(float initialWeight) {
		float previousWeight = initialWeight;
		for (int i = 0; i < 10; ++i) {
			float newWeight = new FQStrategy().getWeightToUse(1, previousWeight);
			Assert.assertTrue(String.format("New weight %s should be greater than previous weight %s", newWeight, previousWeight), newWeight > previousWeight);
			previousWeight = newWeight;
		}
	}
}
