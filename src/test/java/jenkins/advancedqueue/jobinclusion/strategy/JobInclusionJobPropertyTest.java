package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.*;

import hudson.Launcher;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import hudson.util.StreamTaskListener;
import java.io.IOException;
import java.io.PrintStream;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionJobPropertyTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private JobInclusionJobProperty property;
    private FolderBasedJobInclusionStrategy strategy;
    private FreeStyleProject j;
    private DecisionLogger decisionLogger;
    private FreeStyleBuild build;
    private StreamBuildListener listener;
    private Launcher launcher;

    @Before
    public void setUp() throws Exception {
        property = new JobInclusionJobProperty(true, "testGroup");
        strategy = new FolderBasedJobInclusionStrategy("testFolder");
        j = jenkinsRule.createFreeStyleProject("testFolder_jobName");
        jenkinsRule.buildAndAssertSuccess(j);
        build = j.scheduleBuild2(0).get();
        listener = new StreamBuildListener(new PrintStream(System.out));
        launcher = new hudson.Launcher.LocalLauncher(StreamTaskListener.fromStdout());

        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        property = null;
    }

    @Test
    public void getDescriptor() {
        assertNotNull(property.getDescriptor());
    }

    @Test
    public void getJobAction() {
        // Assuming getJobAction returns some action
        assertNotNull(property.getJobActions(j));
    }

    @Test
    public void getJobActions() {
        // Assuming getJobActions returns a list of actions
        assertNotNull(property.getJobActions(j));
    }

    @Test
    public void prebuild() {
        // Assuming prebuild performs some pre-build actions
        assertTrue(property.prebuild(build, listener));
    }

    @Test
    public void perform() throws IOException, InterruptedException {
        // Assuming perform executes some actions
        assertTrue(property.perform(build, launcher, listener));
    }

    @Test
    public void getRequiredMonitorService() {
        // Assuming getRequiredMonitorService returns some service
        assertNotNull(property.getRequiredMonitorService());
    }

    @Test
    public void getProjectActions() {
        // Assuming getProjectAction returns some project action
        assertNotNull(property.getProjectActions(j));
    }

    @Test
    public void getJobGroupNameReturnsCorrectName() throws Exception {
        FreeStyleProject j = jenkinsRule.createFreeStyleProject("test-project");
        JobInclusionJobProperty property = new JobInclusionJobProperty(true, "groupName");
        j.addProperty(property);
        assertEquals("groupName", property.getJobGroupName());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenNotSet() {
        JobInclusionJobProperty property = new JobInclusionJobProperty(false, null);
        assertNull(property.getJobGroupName());
    }

    @Test
    public void isUseJobGroupReturnsCorrectValue() {
        JobInclusionJobProperty property = new JobInclusionJobProperty(true, "groupName");
        assertTrue(property.isUseJobGroup());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenJobGroupNameNotSet() {
        JobInclusionJobProperty property = new JobInclusionJobProperty(true, null);
        assertNull(property.getJobGroupName());
    }

    @Test
    public void descriptorImplGetDisplayName() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertEquals("XXX", descriptor.getDisplayName());
    }

    @Test
    public void descriptorImplIsUsed() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertFalse(descriptor.isUsed());
    }
}
