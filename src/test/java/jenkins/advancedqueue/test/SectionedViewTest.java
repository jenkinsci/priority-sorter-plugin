package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand.CLICause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class SectionedViewTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private JobHelper jobHelper = new JobHelper(j);

    @Test
    @LocalData
    public void sectioned_view_test() throws Exception {
        // Job 2 matches Sectioned View and All -> Sectioned View is before All -> priority is 1
        // Job 1 matches View1 and All -> View1 is before All -> priority is 2
        // Job 0 matched only All -> priority is 3
        TestRunListener.init(new ExpectedItem("Job 2", 1), new ExpectedItem("Job 1", 2), new ExpectedItem("Job 0", 3));
        jobHelper
                .scheduleProjects(new CLICause(), new CLICause(), new CLICause())
                .go();
        j.waitUntilNoActivity();
        TestRunListener.assertStartedItems();
    }
}
