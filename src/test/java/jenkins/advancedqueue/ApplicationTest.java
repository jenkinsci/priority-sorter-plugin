package jenkins.advancedqueue;

import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.FreeStyleProject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.TestRunListener;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class ApplicationTest {

	private final static Logger LOGGER = Logger.getLogger(ApplicationTest.class.getName());
	
	@Rule
	public JenkinsRule j = new JenkinsRule();

	private List<FreeStyleProject> createProjects(int numberOfProjects) throws Exception {
		List<FreeStyleProject> projects = new ArrayList<FreeStyleProject>(numberOfProjects);
		for (int i = 0; i < numberOfProjects; i++) {
			FreeStyleProject project = j.createFreeStyleProject("Job " + i);
			projects.add(project);
		}
		return projects;
	}

	private void scheduleProjects(Cause... causes) throws Exception {
		List<FreeStyleProject> projects = createProjects(causes.length);
		for (int i = 0; i < causes.length; i++) {
			projects.get(i).scheduleBuild(1, causes[i]);
		}
	}

	@Test
	public void simple_with_no_configuration() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 3));
		scheduleProjects(new UserIdCause());
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	public void simple_two_jobs_with_no_configuration() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 3), new ExpectedItem("Job 1", 3));
		scheduleProjects(new UserIdCause(), new UserIdCause());
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}

	@Test
	@LocalData
	public void simple_with_basic_configuration() throws Exception {
		TestRunListener.init(new ExpectedItem("Job 0", 9));
		scheduleProjects(new UserIdCause());
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();		
	}
}
