package jenkins.advancedqueue.test;

import hudson.cli.BuildCommand.CLICause;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class SectionedViewTest {

    private JenkinsRule j;
    private JobHelper jobHelper;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
        jobHelper = new JobHelper(j);
    }

    @Test
    @LocalData
    void sectioned_view_test() throws Exception {
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
