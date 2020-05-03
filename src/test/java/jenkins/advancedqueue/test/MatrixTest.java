package jenkins.advancedqueue.test;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import hudson.cli.BuildCommand.CLICause;
import hudson.model.Cause.UserIdCause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;

public class MatrixTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	private JobHelper jobHelper = new JobHelper(j);

	@Test
	@LocalData
	public void simple_matrix_with_no_configuration() throws Exception {
		TestRunListener.init(
				new ExpectedItem("Matrix 0", 1), 
				new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1)
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
				new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1),
				new ExpectedItem("1A1=1A.", 1), new ExpectedItem("1A1=1A.", 1)
		);
		jobHelper.scheduleMatrixProjects(new UserIdCause(), new UserIdCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}


	@Test
	@LocalData
	@Ignore("Shaky Test: Timing issue when schedule Matrix and non Matrix Jobs. Results in non Matrix job first build near almost 100% propability.")
	public void matrix_and_jobs_with_no_configuration() throws Exception {
		TestRunListener.init(
				new ExpectedItem("Matrix 0", 1), new ExpectedItem("Matrix 1", 5), 
				new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1),
				new ExpectedItem("Job 0", 5),
				new ExpectedItem("1A1=1A.", 5), new ExpectedItem("1A1=1A.", 5)
		);
		jobHelper.scheduleProjects(3, new CLICause()).scheduleMatrixProjects(new UserIdCause(), new CLICause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	@LocalData
	public void matrix_and_jobs_with_no_configuration_reverse() throws Exception {
		TestRunListener.init(
				new ExpectedItem("Matrix 0", 1), new ExpectedItem("Matrix 1", 5), 
				new ExpectedItem("0A1=0A.", 1), new ExpectedItem("0A1=0A.", 1),
				new ExpectedItem("1A1=1A.", 5), new ExpectedItem("1A1=1A.", 5),
				new ExpectedItem("Job 0", 5)
		);
		jobHelper.scheduleMatrixProjects(new UserIdCause(), new CLICause()).scheduleProjects(5, new CLICause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}
}
