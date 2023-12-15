package jenkins.advancedqueue.test;

import static org.junit.Assume.assumeTrue;

import hudson.model.Cause.UserIdCause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class MatrixTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private JobHelper jobHelper = new JobHelper(j);

    @Test
    @LocalData
    public void simple_matrix_with_no_configuration() throws Exception {
        TestRunListener.init(
                new ExpectedItem("Matrix 0", 1), new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1));
        jobHelper.scheduleMatrixProjects(new UserIdCause()).go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }

    private boolean isWindows() {
        return java.io.File.pathSeparatorChar == ';';
    }
}
