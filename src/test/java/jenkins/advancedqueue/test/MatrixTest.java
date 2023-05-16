package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand;
import hudson.model.Cause;
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
            jobHelper.scheduleMatrixProjects(new Cause.UserIdCause()).go();
            j.waitUntilNoActivity();
            TestRunListener.assertStartedItems();
        }

        @Test
        @LocalData
        public void simple_two_matrix_with_no_configuration() throws Exception {
            TestRunListener.init(
                    new ExpectedItem("Matrix 0", 1), new ExpectedItem("Matrix 1", 1),
                    new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1),
                    new ExpectedItem("1A1=1A.", 1), new ExpectedItem("1A1=1A.", 1));
            jobHelper.scheduleMatrixProjects(new Cause.UserIdCause(), new Cause.UserIdCause()).go();
            j.waitUntilNoActivity();
            TestRunListener.assertStartedItems();
        }

        @Test
        @LocalData
        public void matrix_and_jobs_with_no_configuration() throws Exception {
            TestRunListener.init(
                    new ExpectedItem("Matrix 0", 1),
                    new ExpectedItem("Matrix 1", 5),
                    new ExpectedItem("0A1=0A.", 1),
                    new ExpectedItem("0A1=0A.", 1),
                    new ExpectedItem("Job 0", 5),
                    new ExpectedItem("1A1=1A.", 5),
                    new ExpectedItem("1A1=1A.", 5));
            jobHelper
                    .scheduleProjects(new BuildCommand.CLICause())
                    .scheduleMatrixProjects(new Cause.UserIdCause(), new BuildCommand.CLICause())
                    .go();
            j.waitUntilNoActivity();
            TestRunListener.assertStartedItems();
        }

        @Test
        @LocalData
        public void matrix_and_jobs_with_no_configuration_reverse() throws Exception {
            TestRunListener.init(
                    new ExpectedItem("Matrix 0", 1),
                    new ExpectedItem("Matrix 1", 5),
                    new ExpectedItem("0A1=0A.", 1),
                    new ExpectedItem("0A1=0A.", 1),
                    new ExpectedItem("1A1=1A.", 5),
                    new ExpectedItem("1A1=1A.", 5),
                    new ExpectedItem("Job 0", 5));
            jobHelper
                    .scheduleMatrixProjects(new Cause.UserIdCause(), new BuildCommand.CLICause())
                    .scheduleProjects(new BuildCommand.CLICause())
                    .go();
            j.waitUntilNoActivity();
            TestRunListener.assertStartedItems();
        }
}
