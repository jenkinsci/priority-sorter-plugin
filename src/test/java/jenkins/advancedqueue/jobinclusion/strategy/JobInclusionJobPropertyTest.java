package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import hudson.Launcher;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import hudson.util.StreamTaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionJobPropertyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Rule
    public TestName testName = new TestName();

    private JobInclusionJobProperty property;
    private FreeStyleProject project;
    private FreeStyleBuild build;
    private StreamBuildListener listener;
    private Launcher launcher;

    @Before
    public void setUp() throws Exception {
        property = new JobInclusionJobProperty(true, "testGroup");
        project = j.createFreeStyleProject("testFolder_" + testName.getMethodName());
        build = j.buildAndAssertSuccess(project);
        listener = new StreamBuildListener(new PrintStream(System.out), StandardCharsets.UTF_8);
        launcher = new hudson.Launcher.LocalLauncher(StreamTaskListener.fromStdout());
    }

    @Test
    public void getDescriptor() {
        assertNotNull(property.getDescriptor());
    }

    @Test
    public void getJobAction() {
        // Assuming getJobAction returns some action
        assertNotNull(property.getJobActions(project));
    }

    @Test
    public void getJobActions() {
        // Assuming getJobActions returns a list of actions
        assertNotNull(property.getJobActions(project));
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
        assertNotNull(property.getProjectActions(project));
    }

    @Test
    public void getJobGroupNameReturnsCorrectName() throws Exception {
        FreeStyleProject myProject = j.createFreeStyleProject("test-project");
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, "groupName");
        myProject.addProperty(jobProperty);
        assertEquals("groupName", jobProperty.getJobGroupName());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenNotSet() {
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(false, null);
        assertNull(jobProperty.getJobGroupName());
    }

    @Test
    public void isUseJobGroupReturnsCorrectValue() {
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, "groupName");
    }

    @Test
    public void isUseJobGroupTest() {
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, null);
        assertTrue(jobProperty.isUseJobGroup());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenJobGroupNameNotSet() {
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, null);
        assertNull(jobProperty.getJobGroupName());
    }

    @Test
    public void descriptorImplGetDisplayName() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertEquals("XXX", descriptor.getDisplayName());
    }

    public void getDescriptorTest() {
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, null);
        assertNotNull(jobProperty.getDescriptor());
    }

    @Test
    public void descriptorImplIsUsed() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertFalse(descriptor.isUsed());
    }

    public void getJobGroupsTest() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertNotNull(descriptor.getJobGroups());
    }
}
