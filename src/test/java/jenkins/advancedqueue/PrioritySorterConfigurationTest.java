package jenkins.advancedqueue;

import org.junit.Assert;
import org.junit.Test;

public class PrioritySorterConfigurationTest {

	@Test
	public void testNormalizedOffset() {
		Assert.assertEquals(1, PrioritySorterConfiguration.normalizedOffset(0));
		Assert.assertEquals(11,
				PrioritySorterConfiguration.normalizedOffset(-10));
		Assert.assertEquals(-9,
				PrioritySorterConfiguration.normalizedOffset(10));
	}

	@Test
	public void testInverseAndNormalize() {
		Assert.assertEquals(1,
				PrioritySorterConfiguration.inverseAndNormalize(0, 100, 100));
		Assert.assertEquals(101,
				PrioritySorterConfiguration.inverseAndNormalize(0, 100, 0));
		Assert.assertEquals(51,
				PrioritySorterConfiguration.inverseAndNormalize(0, 100, 50));
		Assert.assertEquals(52,
				PrioritySorterConfiguration.inverseAndNormalize(0, 100, 49));

		Assert.assertEquals(1,
				PrioritySorterConfiguration.inverseAndNormalize(-10, 10, 10));
		Assert.assertEquals(21,
				PrioritySorterConfiguration.inverseAndNormalize(-10, 10, -10));
		Assert.assertEquals(11,
				PrioritySorterConfiguration.inverseAndNormalize(-10, 10, 0));
	}

	@Test
	public void testLegacyPriorityToAdvancedPriority() {
		Assert.assertEquals(1, PrioritySorterConfiguration
				.legacyPriorityToAdvancedPriority(100, 200, 10, 200));
		Assert.assertEquals(10, PrioritySorterConfiguration
				.legacyPriorityToAdvancedPriority(100, 200, 10, 100));
		Assert.assertEquals(5, PrioritySorterConfiguration
				.legacyPriorityToAdvancedPriority(100, 200, 10, 150));

		Assert.assertEquals(1, PrioritySorterConfiguration
				.legacyPriorityToAdvancedPriority(-10, 10, 10, 10));
		Assert.assertEquals(10, PrioritySorterConfiguration
				.legacyPriorityToAdvancedPriority(-10, 10, 10, -10));
	}

}
