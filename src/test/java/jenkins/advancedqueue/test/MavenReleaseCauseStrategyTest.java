package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand;
import hudson.model.Cause.UserIdCause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.plugins.m2release.ReleaseCause;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class MavenReleaseCauseStrategyTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	private JobHelper jobHelper = new JobHelper(j);

	@Test
	@LocalData
	public void simple_with_no_configuration() throws Exception {
		TestRunListener.init(//
				new ExpectedItem("Job 0", 1), //
				new ExpectedItem("Job 1", 2), //
				new ExpectedItem("Job 2", 5)// <- Maven Release Job uses configured prio
		);

		jobHelper.scheduleProjects(new UserIdCause(), new BuildCommand.CLICause(), new ReleaseCause()).go();
		j.waitUntilNoActivity();
		TestRunListener.assertStartedItems();
	}

}
