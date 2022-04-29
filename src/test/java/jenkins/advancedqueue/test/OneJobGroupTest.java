package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand.CLICause;
import hudson.model.Cause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class OneJobGroupTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	private JobHelper jobHelper = new JobHelper(j);

	@Test
	@LocalData
	public void default_job_group_priority() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 3));
		jobHelper.scheduleProjects(new Cause() {

			@Override
			public String getShortDescription() {
				return "Dummy Cause";
			}
		}).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();
	}

	@Test
	@LocalData
	public void test_UserIdCause() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 4));
		jobHelper.scheduleProjects(new Cause.UserIdCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();
	}

	@Test
	@LocalData
	public void test_CLICause() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 5));
		jobHelper.scheduleProjects(new CLICause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();
	}

	@Test
	@LocalData
	public void test_multiple_strategies() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 2", 3), new ExpectedItem("Job 1", 4), new ExpectedItem("Job 0", 5));
		jobHelper.scheduleProjects(new CLICause(), new Cause.UserIdCause(), new Cause() {
			@Override
			public String getShortDescription() {
				return "Dummy Cause";
			}
		}).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();
	}

}
