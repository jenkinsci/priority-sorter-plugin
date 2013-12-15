package jenkins.advancedqueue.testutil;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.hudson.test.JenkinsRule;

public class JobHelper {

	public JenkinsRule j;

	public JobHelper(JenkinsRule j) {
		this.j = j;
	}

	public List<FreeStyleProject> createProjects(int numberOfProjects) throws Exception {
		List<FreeStyleProject> projects = new ArrayList<FreeStyleProject>(numberOfProjects);
		for (int i = 0; i < numberOfProjects; i++) {
			FreeStyleProject project = j.createFreeStyleProject("Job " + i);
			projects.add(project);
		}
		return projects;
	}

	public void scheduleProjects(Cause... causes) throws Exception {
		List<FreeStyleProject> projects = createProjects(causes.length);
		for (int i = 0; i < causes.length; i++) {
			projects.get(i).scheduleBuild(2, causes[i]);
			Thread.sleep(100);
		}
	}

}
