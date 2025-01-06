package jenkins.advancedqueue.priority.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.StringParameterValue;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildParameterStrategyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject project;
    private static BuildParameterStrategy strategy;

    @BeforeClass
    public static void setUp() throws IOException {
        project = j.createFreeStyleProject();
        strategy = new BuildParameterStrategy("priority");
    }

    @Test
    public void getPriority_returnsPriorityFromParameter() {
        StringParameterValue param = new StringParameterValue("priority", "5");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        int priority = strategy.getPriority(item);

        assertEquals(5, priority);
    }

    @Test
    public void getPriority_returnsDefaultPriorityWhenParameterIsMissing() {
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());

        int priority = strategy.getPriority(item);

        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), priority);
    }

    @Test
    public void getPriority_returnsDefaultPriorityWhenParameterIsNotANumber() {
        StringParameterValue param = new StringParameterValue("priority", "not-a-number");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        int priority = strategy.getPriority(item);

        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), priority);
    }

    @Test
    public void isApplicable_returnsTrueWhenParameterIsPresentAndValid() {
        StringParameterValue param = new StringParameterValue("priority", "5");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        assertTrue(strategy.isApplicable(item));
    }

    @Test
    public void isApplicable_returnsFalseWhenParameterIsMissing() {
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());

        assertFalse(strategy.isApplicable(item));
    }

    @Test
    public void isApplicable_returnsFalseWhenParameterIsNotANumber() {
        StringParameterValue param = new StringParameterValue("priority", "not-a-number");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        assertFalse(strategy.isApplicable(item));
    }
}
