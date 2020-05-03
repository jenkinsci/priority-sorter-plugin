package jenkins.advancedqueue.test;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import hudson.cli.BuildCommand.CLICause;
import hudson.model.Cause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;

public class OneJobGroupTest {

	private static class DummyCause extends Cause implements Serializable {
		private static final long serialVersionUID = -163734048850160596L;
		private String description;
		public DummyCause(String description) { this.description = description; }
		@Override
		public String getShortDescription() { return description; }
	}

	@Rule
	public JenkinsRule j = new JenkinsRule();

	private JobHelper jobHelper = new JobHelper(j);

	@Test
	@LocalData
	public void default_job_group_priority() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 3));
		jobHelper.scheduleProjects(new DummyCause("Dummy Cause")).go();
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
		jobHelper.scheduleProjects(new CLICause(), new Cause.UserIdCause(), new DummyCause("Dummy Cause")).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();
	}

}
