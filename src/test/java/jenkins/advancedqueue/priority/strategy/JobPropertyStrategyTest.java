package jenkins.advancedqueue.priority.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import hudson.model.Queue;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobPropertyStrategyTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void isApplicableTestWhengetPriorityInternalReturnsNull() {
        JobPropertyStrategy strategy = new JobPropertyStrategy();
        Queue.Item mockItem = mock(Queue.Item.class);
        boolean result = strategy.isApplicable(mockItem);
        // the result is false since mockItem.task instanceof Job<?, ?> does not hold
        assertFalse(result);
    }

    @Test
    public void getPriorityTestWhengetPriorityInternalReturnsNull() {
        JobPropertyStrategy strategy = new JobPropertyStrategy();
        Queue.Item mockItem = mock(Queue.Item.class);
        int priority = strategy.getPriority(mockItem);
        assertNotNull(priority);
        // priority is 3 when mockItem.task instanceof Job<?, ?> does not hold
        assertEquals(priority, 3);
    }
}
