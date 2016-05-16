package jenkins.advancedqueue.sorter.strategy;

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
		Assert.assertEquals(1.00000F + FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(1, 1.00000F), 0F);
		Assert.assertEquals(1.00001F + FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(1, 1.00001F), 0F);
		Assert.assertEquals(1.00000F + 2*FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(2, 1.00000F), 0F);
		Assert.assertEquals(1.00001F + 2*FQBaseStrategy.MIN_STEP_SIZE, new WFQStrategy().getWeightToUse(2, 1.00001F), 0F);
	}
	
}
