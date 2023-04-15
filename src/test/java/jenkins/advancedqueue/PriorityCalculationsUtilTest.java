package jenkins.advancedqueue;

import org.junit.Assert;
import org.junit.Test;

public class PriorityCalculationsUtilTest {

    @Test
    public void testScale() {
        Assert.assertEquals(10, PriorityCalculationsUtil.scale(100, 10, 100));
        Assert.assertEquals(1, PriorityCalculationsUtil.scale(100, 10, 9));
        Assert.assertEquals(5, PriorityCalculationsUtil.scale(100, 10, 50));
        Assert.assertEquals(8, PriorityCalculationsUtil.scale(100, 10, 75));

        Assert.assertEquals(1, PriorityCalculationsUtil.scale(5, 10, 1));
        Assert.assertEquals(3, PriorityCalculationsUtil.scale(5, 10, 2));
        Assert.assertEquals(5, PriorityCalculationsUtil.scale(5, 10, 3));
        Assert.assertEquals(8, PriorityCalculationsUtil.scale(5, 10, 4));
        Assert.assertEquals(10, PriorityCalculationsUtil.scale(5, 10, 5));
    }

    @Test
    public void testScaleUseDefaultPriority() {
        Assert.assertEquals(
                PriorityCalculationsUtil.getUseDefaultPriorityPriority(),
                PriorityCalculationsUtil.scale(5, 10, PriorityCalculationsUtil.getUseDefaultPriorityPriority()));
    }
}
