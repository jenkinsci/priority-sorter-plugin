package jenkins.advancedqueue.priority.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobPropertyStrategyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject project;
    private static FreeStyleProject projectWithProperty;
    private static FreeStyleProject projectWithUnusedProperty;

    private static int defaultPriority;
    private static int jobPriority;

    private JobPropertyStrategy strategy;

    @BeforeClass
    public static void startProject() throws Exception {
        project = j.createFreeStyleProject("no-property");
        // Schedule initial delay so job is queued but does not run
        project.scheduleBuild2(600);
    }

    @BeforeClass
    public static void startProjectWithProperty() throws Exception {
        boolean useJobPriority = true;
        PriorityJobProperty property = new PriorityJobProperty(useJobPriority, jobPriority);
        projectWithProperty = j.createFreeStyleProject("with-property");
        projectWithProperty.addProperty(property);
        // Schedule initial delay so job is queued but does not run
        projectWithProperty.scheduleBuild2(600);
    }

    @BeforeClass
    public static void startProjectWithUnusedProperty() throws Exception {
        boolean useJobPriority = false;
        PriorityJobProperty property = new PriorityJobProperty(useJobPriority, jobPriority);
        projectWithUnusedProperty = j.createFreeStyleProject("with-unused-property");
        projectWithUnusedProperty.addProperty(property);
        // Schedule initial delay so job is queued but does not run
        projectWithUnusedProperty.scheduleBuild2(600);
    }

    @BeforeClass
    public static void setPriorities() {
        defaultPriority = PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
        jobPriority = defaultPriority - 1;
    }

    @Before
    public void createStrategy() {
        strategy = new JobPropertyStrategy();
    }

    @Test
    public void isApplicableTestWhengetPriorityInternalReturnsNull() {
        Queue.Item nullItem = null;
        assertFalse(strategy.isApplicable(nullItem));
        assertFalse(strategy.isApplicable(null));
    }

    @Test
    public void isApplicableWhenFreeStyleProject() {
        assertFalse(strategy.isApplicable(project.getQueueItem()));
    }

    @Test
    public void isApplicableWhenFreeStyleProjectWithPriorityProperty() {
        assertTrue(strategy.isApplicable(projectWithProperty.getQueueItem()));
    }

    @Test
    public void isApplicableWhenFreeStyleProjectWithUnusedPriorityProperty() {
        assertFalse(strategy.isApplicable(projectWithUnusedProperty.getQueueItem()));
    }

    @Test
    public void getPriorityTestWhengetPriorityInternalReturnsNull() {
        Queue.Item nullItem = null;
        // priority is 3 when item is null
        assertThat(strategy.getPriority(nullItem), is(defaultPriority));
        assertThat(strategy.getPriority(null), is(defaultPriority));
    }

    @Test
    public void getPriorityTestFreeStyleProject() {
        assertThat(strategy.getPriority(project.getQueueItem()), is(defaultPriority));
    }

    @Test
    public void getPriorityTestFreeStyleProjectWithPriorityProperty() {
        assertThat(strategy.getPriority(projectWithProperty.getQueueItem()), is(jobPriority));
    }

    @Test
    public void getPriorityTestFreeStyleProjectWithUnusedPriorityProperty() {
        assertThat(strategy.getPriority(projectWithUnusedProperty.getQueueItem()), is(defaultPriority));
    }
}
