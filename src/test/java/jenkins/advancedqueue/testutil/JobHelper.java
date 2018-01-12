package jenkins.advancedqueue.testutil;

import hudson.Launcher;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.jvnet.hudson.test.JenkinsRule;

public class JobHelper {

	private final static Logger LOGGER = Logger.getLogger(JobHelper.class.getName());

	public JenkinsRule j;

	public JobHelper(JenkinsRule j) {
		this.j = j;
	}

	static class TestBuilder extends Builder {

		private int sleepTime;

		public TestBuilder(int sleepTime) {
			this.sleepTime = sleepTime;
		}

		@Override
		public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
				throws InterruptedException, IOException {
			LOGGER.info("Building: " + build.getParent().getName());
			Thread.sleep(sleepTime);
			return true;
		}
	}

	public FreeStyleProject createProject(String name) throws Exception {
		FreeStyleProject project = j.createFreeStyleProject(name);
		project.getBuildersList().add(new TestBuilder(100));
		return project;
	}

	public List<FreeStyleProject> createProjects(int numberOfProjects) throws Exception {
		List<FreeStyleProject> projects = new ArrayList<FreeStyleProject>(numberOfProjects);
		for (int i = 0; i < numberOfProjects; i++) {
			FreeStyleProject project = j.createFreeStyleProject("Job " + i);
			project.getBuildersList().add(new TestBuilder(100));
			projects.add(project);
		}
		return projects;
	}

	public List<MatrixProject> createMatrixProjects(int numberOfProjects) throws Exception {
		List<MatrixProject> projects = new ArrayList<MatrixProject>(numberOfProjects);
		for (int i = 0; i < numberOfProjects; i++) {
			MatrixProject project = j.createProject(MatrixProject.class, "Matrix " + i);
			project.getBuildersList().add(new TestBuilder(100));
	        AxisList axes = new AxisList();
	        axes.add(new TextAxis(i + "A1", i + "A2", i + "A3"));
	        project.setAxes(axes);
			projects.add(project);
		}
		return projects;
	}

	public JobHelper scheduleMatrixProjects(Cause... causes) throws Exception {
		List<MatrixProject> projects = createMatrixProjects(causes.length);
		// Scheduling executors is zero
		for (int i = 0; i < causes.length; i++) {
			projects.get(i).scheduleBuild(0, causes[i]);
			Thread.sleep(100);
		}
		return this;
	}

	public JobHelper scheduleProject(String name, Cause cause) throws Exception {
		FreeStyleProject project = createProject(name);
		// Scheduling executors is zero
		project.scheduleBuild(0, cause);
		Thread.sleep(100);
		return this;
	}

	public JobHelper scheduleProjects(Cause... causes) throws Exception {
		List<FreeStyleProject> projects = createProjects(causes.length);
		// Scheduling executors is zero
		for (int i = 0; i < causes.length; i++) {
			projects.get(i).scheduleBuild(0, causes[i]);
			Thread.sleep(100);
		}
		return this;
	}

	public void go() throws Exception {
		// Set the executors to one and restart
		Jenkins.getInstance().setNumExecutors(1);
		// TODO: is there any other way to make the 1 take effect than a reload?
		Jenkins.getInstance().reload();
	}

}
