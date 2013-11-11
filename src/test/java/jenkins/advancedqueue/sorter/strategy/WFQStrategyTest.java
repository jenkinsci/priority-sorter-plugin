package jenkins.advancedqueue.sorter.strategy;

import jenkins.advancedqueue.sorter.strategy.FQBaseStrategy;
import jenkins.advancedqueue.sorter.strategy.WFQStrategy;

import org.junit.Assert;
import org.junit.Test;

public class WFQStrategyTest {

	@Test
	public void testStepSize() {
		Assert.assertEquals(1 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(1), 0F);
		Assert.assertEquals(2 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(2), 0F);
		Assert.assertEquals(3 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(3), 0F);
		Assert.assertEquals(4 * FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getStepSize(4), 0F);
	}
	
	
	@Test
	public void testGetWeightToUse() {
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE * 1 * 456456, new WFQStrategy().getWeightToUse(1, 4.56455F), 0F);
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE * 2 * 228228, new WFQStrategy().getWeightToUse(2, 4.56455F), 0F);
		Assert.assertEquals(FQBaseStrategy.MIN_STEP_SIZE * 3 * 152152, new WFQStrategy().getWeightToUse(3, 4.56455F), 0F);
	}

	
}
