package jenkins.advancedqueue.priority.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JobPropertyStrategyTest {

    private static JenkinsRule j;

    private static FreeStyleProject project;
    private static FreeStyleProject projectWithProperty;
    private static FreeStyleProject projectWithUnusedProperty;

    private static int defaultPriority;
    private static int jobPriority;

    private JobPropertyStrategy strategy;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;

        defaultPriority = PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
        jobPriority = defaultPriority - 1;

        project = j.createFreeStyleProject("no-property");
        // Schedule initial delay so job is queued but does not run
        project.scheduleBuild2(600);

        PriorityJobProperty withProperty = new PriorityJobProperty(true, jobPriority);
        projectWithProperty = j.createFreeStyleProject("with-property");
        projectWithProperty.addProperty(withProperty);
        // Schedule initial delay so job is queued but does not run
        projectWithProperty.scheduleBuild2(600);

        PriorityJobProperty withoutProperty = new PriorityJobProperty(false, jobPriority);
        projectWithUnusedProperty = j.createFreeStyleProject("with-unused-property");
        projectWithUnusedProperty.addProperty(withoutProperty);
        // Schedule initial delay so job is queued but does not run
        projectWithUnusedProperty.scheduleBuild2(600);
    }

    @BeforeEach
    void beforeEach() {
        strategy = new JobPropertyStrategy();
    }

    @Test
    void isApplicableTestWhengetPriorityInternalReturnsNull() {
        Queue.Item nullItem = null;
        assertFalse(strategy.isApplicable(nullItem));
        assertFalse(strategy.isApplicable(null));
    }

    @Test
    void isApplicableWhenFreeStyleProject() {
        assertFalse(strategy.isApplicable(project.getQueueItem()));
    }

    @Test
    void isApplicableWhenFreeStyleProjectWithPriorityProperty() {
        assertTrue(strategy.isApplicable(projectWithProperty.getQueueItem()));
    }

    @Test
    void isApplicableWhenFreeStyleProjectWithUnusedPriorityProperty() {
        assertFalse(strategy.isApplicable(projectWithUnusedProperty.getQueueItem()));
    }

    @Test
    void getPriorityTestWhengetPriorityInternalReturnsNull() {
        Queue.Item nullItem = null;
        // priority is 3 when item is null
        assertThat(strategy.getPriority(nullItem), is(defaultPriority));
        assertThat(strategy.getPriority(null), is(defaultPriority));
    }

    @Test
    void getPriorityTestFreeStyleProject() {
        assertThat(strategy.getPriority(project.getQueueItem()), is(defaultPriority));
    }

    @Test
    void getPriorityTestFreeStyleProjectWithPriorityProperty() {
        assertThat(strategy.getPriority(projectWithProperty.getQueueItem()), is(jobPriority));
    }

    @Test
    void getPriorityTestFreeStyleProjectWithUnusedPriorityProperty() {
        assertThat(strategy.getPriority(projectWithUnusedProperty.getQueueItem()), is(defaultPriority));
    }
}
