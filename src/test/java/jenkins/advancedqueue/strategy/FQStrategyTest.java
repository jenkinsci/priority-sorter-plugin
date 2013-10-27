package jenkins.advancedqueue.strategy;

import jenkins.advancedqueue.sorter.strategy.FQBaseStrategy;
import jenkins.advancedqueue.sorter.strategy.FQStrategy;

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
		Assert.assertEquals(4.56456F + FQBaseStrategy.MIN_STEP_SIZE, new FQStrategy().getWeightToUse(2, 4.56456F), 0F);
	}

}
