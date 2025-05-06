package jenkins.advancedqueue.test;

import hudson.model.Cause.UserIdCause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class JobPatternGroupTest {

    private JenkinsRule j;
    private JobHelper jobHelper;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
        jobHelper = new JobHelper(j);
    }

    @Test
    @LocalData
    void test_job_pattern_1() throws Exception {
        TestRunListener.init(new ExpectedItem("Job 0", 3));
        jobHelper.scheduleProjects(new UserIdCause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    void test_job_pattern_2() throws Exception {
        TestRunListener.init(new ExpectedItem("Job 0", 3), new ExpectedItem("Job 1", 9));
        jobHelper.scheduleProjects(new UserIdCause(), new UserIdCause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    void test_job_pattern_3() throws Exception {
        TestRunListener.init(
                new ExpectedItem("Job 0", 3),
                new ExpectedItem("Job 3", 3),
                new ExpectedItem("Job 1", 9),
                new ExpectedItem("Job 2", 9));
        jobHelper
                .scheduleProjects(new UserIdCause(), new UserIdCause(), new UserIdCause(), new UserIdCause())
                .go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }
}
