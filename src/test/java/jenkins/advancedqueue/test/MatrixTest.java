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
class MatrixTest {

    private JenkinsRule j;
    private JobHelper jobHelper;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
        jobHelper = new JobHelper(j);
    }

    @Test
    @LocalData
    void simple_matrix_with_no_configuration() throws Exception {
        TestRunListener.init(
                new ExpectedItem("Matrix 0", 1), new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1));
        jobHelper.scheduleMatrixProjects(new UserIdCause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }
}
