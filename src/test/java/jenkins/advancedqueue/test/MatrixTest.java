package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand.CLICause;
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
				new ExpectedItem("Matrix 0", 1), 
				new ExpectedItem("Matrix 0 » 0A.", 1), new ExpectedItem("Matrix 0 » 0A.", 1)
		);
		jobHelper.scheduleMatrixProjects(new UserIdCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	@LocalData
	public void simple_two_matrix_with_no_configuration() throws Exception {
		TestRunListener.init(
				new ExpectedItem("Matrix 0", 1), new ExpectedItem("Matrix 1", 1), 
				new ExpectedItem("Matrix 0 » 0A.", 1), new ExpectedItem("Matrix 0 » 0A.", 1),
				new ExpectedItem("Matrix 1 » 1A.", 1), new ExpectedItem("Matrix 1 » 1A.", 1)
		);
		jobHelper.scheduleMatrixProjects(new UserIdCause(), new UserIdCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	@LocalData
	public void matrix_and_jobs_with_no_configuration() throws Exception {
		TestRunListener.init(
				new ExpectedItem("Matrix 0", 1), new ExpectedItem("Matrix 1", 5), 
				new ExpectedItem("Matrix 0 » 0A.", 1), new ExpectedItem("Matrix 0 » 0A.", 1),
				new ExpectedItem("Job 0", 5),
				new ExpectedItem("Matrix 1 » 1A.", 5), new ExpectedItem("Matrix 1 » 1A.", 5)
		);
		jobHelper.scheduleProjects(new CLICause()).scheduleMatrixProjects(new UserIdCause(), new CLICause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	@LocalData
	public void matrix_and_jobs_with_no_configuration_reverse() throws Exception {
		TestRunListener.init(
				new ExpectedItem("Matrix 0", 1), new ExpectedItem("Matrix 1", 5), 
				new ExpectedItem("Matrix 0 » 0A.", 1), new ExpectedItem("Matrix 0 » 0A.", 1),
				new ExpectedItem("Matrix 1 » 1A.", 5), new ExpectedItem("Matrix 1 » 1A.", 5),
				new ExpectedItem("Job 0", 5)
		);
		jobHelper.scheduleMatrixProjects(new UserIdCause(), new CLICause()).scheduleProjects(new CLICause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}
}
