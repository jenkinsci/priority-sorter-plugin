package jenkins.advancedqueue;

import org.junit.Assert;
import org.junit.Test;

public class PrioritySorterConfigurationTest {

	@Test
	public void testNormalizedOffset() {
		Assert.assertEquals(  1, PrioritySorterConfiguration.normalizedOffset(0, 100));
		Assert.assertEquals( 11, PrioritySorterConfiguration.normalizedOffset(-10, 10));
	}

	@Test
	public void testInverseAndNormalize() {
		Assert.assertEquals(  1, PrioritySorterConfiguration.inverseAndNormalize(0, 100, 100));
		Assert.assertEquals(101, PrioritySorterConfiguration.inverseAndNormalize(0, 100, 0));
		Assert.assertEquals( 51, PrioritySorterConfiguration.inverseAndNormalize(0, 100, 50));
		Assert.assertEquals( 52, PrioritySorterConfiguration.inverseAndNormalize(0, 100, 49));

		Assert.assertEquals(  1, PrioritySorterConfiguration.inverseAndNormalize(-10, 10, 10));
		Assert.assertEquals( 21, PrioritySorterConfiguration.inverseAndNormalize(-10, 10, -10));
		Assert.assertEquals( 11, PrioritySorterConfiguration.inverseAndNormalize(-10, 10, 0));
	}

	@Test
	public void testScale() {
		Assert.assertEquals( 10, PrioritySorterConfiguration.scale(100, 10, 100));
		Assert.assertEquals(  1, PrioritySorterConfiguration.scale(100, 10, 9));
		Assert.assertEquals(  5, PrioritySorterConfiguration.scale(100, 10, 50));
		Assert.assertEquals(  8, PrioritySorterConfiguration.scale(100, 10, 75));

		Assert.assertEquals(  1, PrioritySorterConfiguration.scale(5, 10, 1));
		Assert.assertEquals(  3, PrioritySorterConfiguration.scale(5, 10, 2));
		Assert.assertEquals(  5, PrioritySorterConfiguration.scale(5, 10, 3));
		Assert.assertEquals(  8, PrioritySorterConfiguration.scale(5, 10, 4));
		Assert.assertEquals( 10, PrioritySorterConfiguration.scale(5, 10, 5));
	}
	
	@Test
	public void testScaleUseDefaultPriority() {
		Assert.assertEquals(PrioritySorterConfiguration.PRIORITY_USE_DEFAULT_PRIORITY, 
				PrioritySorterConfiguration.scale(5, 10, 
						PrioritySorterConfiguration.PRIORITY_USE_DEFAULT_PRIORITY));
	}
}
