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
class MultipleMatchJobGroupTest {

    private JenkinsRule j;
    private JobHelper jobHelper;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
        jobHelper = new JobHelper(j);
    }

    @Test
    @LocalData
    void multiple_job_group_matches() throws Exception {
        // Job 2 and 3 matches View1 and All -> View1 is before All -> priorities are 1 and 2
        // Job 0 and 1 matched only All -> priorities are 3 and 4
        TestRunListener.init(
                new ExpectedItem("Job 2", 1),
                new ExpectedItem("Job 3", 2),
                new ExpectedItem("Job 0", 3),
                new ExpectedItem("Job 1", 4));
        jobHelper
                .scheduleProjects(new CLICause(), new Cause.UserIdCause(), new CLICause(), new Cause.UserIdCause())
                .go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }
}
