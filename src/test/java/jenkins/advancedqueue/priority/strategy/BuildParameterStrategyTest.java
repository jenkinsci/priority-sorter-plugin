package jenkins.advancedqueue.priority.strategy;

import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.StringParameterValue;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildParameterStrategyTest {

    private static JenkinsRule j;

    private static FreeStyleProject project;
    private static BuildParameterStrategy strategy;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws IOException {
        j = rule;
        project = j.createFreeStyleProject();
        strategy = new BuildParameterStrategy("priority");
    }

    @Test
    void getPriority_returnsPriorityFromParameter() {
        StringParameterValue param = new StringParameterValue("priority", "5");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        int priority = strategy.getPriority(item);

        assertEquals(5, priority);
    }

    @Test
    void getPriority_returnsDefaultPriorityWhenParameterIsMissing() {
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());

        int priority = strategy.getPriority(item);

        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), priority);
    }

    @Test
    void getPriority_returnsDefaultPriorityWhenParameterIsNotANumber() {
        StringParameterValue param = new StringParameterValue("priority", "not-a-number");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        int priority = strategy.getPriority(item);

        assertEquals(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority(), priority);
    }

    @Test
    void isApplicable_returnsTrueWhenParameterIsPresentAndValid() {
        StringParameterValue param = new StringParameterValue("priority", "5");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        assertTrue(strategy.isApplicable(item));
    }

    @Test
    void isApplicable_returnsFalseWhenParameterIsMissing() {
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());

        assertFalse(strategy.isApplicable(item));
    }

    @Test
    void isApplicable_returnsFalseWhenParameterIsNotANumber() {
        StringParameterValue param = new StringParameterValue("priority", "not-a-number");
        ParametersAction action = new ParametersAction(param);
        Queue.Item item = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.singletonList(action));

        assertFalse(strategy.isApplicable(item));
    }
}
