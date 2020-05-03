package jenkins.advancedqueue.test;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import hudson.model.Cause.UserIdCause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;

public class SimpleTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	private JobHelper jobHelper = new JobHelper(j);
	
	@Test
	@LocalData
	public void simple_with_no_configuration() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 3));
		jobHelper.scheduleProjects(new UserIdCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	@LocalData
	public void simple_two_jobs_with_no_configuration() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 3), new ExpectedItem("Job 1", 3));
		jobHelper.scheduleProjects(new UserIdCause(), new UserIdCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

}
