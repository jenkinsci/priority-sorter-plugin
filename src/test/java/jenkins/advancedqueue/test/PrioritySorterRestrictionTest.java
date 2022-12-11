package jenkins.advancedqueue.test;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class PrioritySorterRestrictionTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @Parameters({
            "1, 1",
            "10. 10",
            "-15, -15",
            "-10, -10" })
    public void doUpdateFromPriorityItemsTest(int a, int expected){
        assertEquals(a, expectedValue);
    }




}
