package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand.CLICause;
import hudson.model.Cause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class OneJobGroupTest {

    private JenkinsRule j;
    private JobHelper jobHelper;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
        jobHelper = new JobHelper(j);
    }

    @Test
    @LocalData
    void default_job_group_priority() throws Exception {
        TestRunListener.init(new ExpectedItem("Job 0", 3));
        jobHelper
                .scheduleProjects(new Cause() {

                    @Override
                    public String getShortDescription() {
                        return "Dummy Cause";
                    }
                })
                .go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    void test_UserIdCause() throws Exception {
        TestRunListener.init(new ExpectedItem("Job 0", 4));
        jobHelper.scheduleProjects(new Cause.UserIdCause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    void test_CLICause() throws Exception {
        TestRunListener.init(new ExpectedItem("Job 0", 5));
        jobHelper.scheduleProjects(new CLICause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    void test_multiple_strategies() throws Exception {
        TestRunListener.init(new ExpectedItem("Job 2", 3), new ExpectedItem("Job 1", 4), new ExpectedItem("Job 0", 5));
        jobHelper
                .scheduleProjects(new CLICause(), new Cause.UserIdCause(), new Cause() {
                    @Override
                    public String getShortDescription() {
                        return "Dummy Cause";
                    }
                })
                .go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }
}
