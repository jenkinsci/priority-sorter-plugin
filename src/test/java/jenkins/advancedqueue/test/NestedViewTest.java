package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand.CLICause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class NestedViewTest {

    private JenkinsRule j;
    private JobHelper jobHelper;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
        jobHelper = new JobHelper(j);
    }

    @Test
    @LocalData
    void nested_view_test() throws Exception {
        // Job 0 matches "Nested View A/Nested View B" -> priority is 1
        // Job 0 matched nothing -> default priority is 9
        TestRunListener.init(new ExpectedItem("Job 0", 1), new ExpectedItem("Job 1", 9));
        jobHelper.scheduleProjects(new CLICause(), new CLICause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }
}
