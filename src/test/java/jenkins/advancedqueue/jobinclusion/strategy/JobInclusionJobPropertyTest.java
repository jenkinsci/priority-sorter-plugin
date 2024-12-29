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
import hudson.util.ListBoxModel;
import hudson.util.StreamTaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import jenkins.advancedqueue.PriorityConfiguration;
import org.junit.After;
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

    @After
    public void deleteProject() throws Exception {
        project.delete();
    }

    @Test
    public void getDescriptor() {
        JobInclusionJobProperty.DescriptorImpl descriptor = property.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("XXX", descriptor.getDisplayName());
        assertTrue(descriptor instanceof JobInclusionJobProperty.DescriptorImpl);
    }

    @Test
    public void getJobActions() {
        // Setup: Add a job group to the PriorityConfiguration
        PriorityConfiguration.get().getJobGroups().clear(); // Clear existing job groups
        PropertyBasedJobInclusionStrategy.addJobGroup("testJobGroup", "testGroup");

        // Retrieve job actions
        ListBoxModel jobActions = PropertyBasedJobInclusionStrategy.getJobGroups();
        assertNotNull(jobActions);
        assertFalse(jobActions.isEmpty());

        // Check the size of the list
        assertEquals("Expected number of job actions", 1, jobActions.size());

        // Check the properties of the first element
        ListBoxModel.Option firstOption = jobActions.get(0);
        assertNotNull(firstOption);
        assertEquals("Expected display name", "testJobGroup", firstOption.name);
        assertEquals("Expected value", "testGroup", firstOption.value);
    }

    @Test
    public void prebuild() {
        // Assuming prebuild performs some pre-build actions
        assertTrue(property.prebuild(build, listener));

        // Additional assertions to verify the state after prebuild
        assertNotNull(build);
        assertNotNull(listener);
        assertEquals(
                "testFolder_" + testName.getMethodName(), build.getProject().getName());
    }

    @Test
    public void perform() throws IOException, InterruptedException {
        // Assuming perform executes some actions
        assertTrue(property.perform(build, launcher, listener));

        // Additional assertions to verify the state after perform
        assertNotNull(build);
        assertNotNull(launcher);
        assertNotNull(listener);
        assertEquals(
                "testFolder_" + testName.getMethodName(), build.getProject().getName());
    }

    @Test
    public void getRequiredMonitorService() {
        // Assuming getRequiredMonitorService returns some service
        assertNotNull(property.getRequiredMonitorService());
    }

    @Test
    public void getJobGroupNameReturnsCorrectName() throws Exception {
        FreeStyleProject myProject = j.createFreeStyleProject("test-project");
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, "testJobGroupName");
        myProject.addProperty(jobProperty);
        assertEquals("testJobGroupName", jobProperty.getJobGroupName());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenNotSet() {
        // Create a JobInclusionJobProperty with useJobGroup set to false and jobGroupName set to null
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(false, null);
        assertFalse(jobProperty.isUseJobGroup());
        assertNull(jobProperty.getJobGroupName());

        // Create a JobInclusionJobProperty with useJobGroup set to true and jobGroupName set to null
        JobInclusionJobProperty jobPropertyTrue = new JobInclusionJobProperty(true, null);
        assertTrue(jobPropertyTrue.isUseJobGroup());
        assertNull(jobPropertyTrue.getJobGroupName());

        // Create a JobInclusionJobProperty with useJobGroup set to false and jobGroupName set to a non-null value
        JobInclusionJobProperty jobPropertyWithGroupName = new JobInclusionJobProperty(false, "testJobGroupName");
        assertFalse(jobPropertyWithGroupName.isUseJobGroup());
        assertEquals("testJobGroupName", jobPropertyWithGroupName.getJobGroupName());
    }

    @Test
    public void isUseJobGroupReturnsCorrectValue() {
        // Create a JobInclusionJobProperty with useJobGroup set to true
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, "groupName");
        assertTrue(jobProperty.isUseJobGroup());
        assertEquals("groupName", jobProperty.getJobGroupName());

        // Create a JobInclusionJobProperty with useJobGroup set to false
        JobInclusionJobProperty jobPropertyFalse = new JobInclusionJobProperty(false, "groupName");
        assertFalse(jobPropertyFalse.isUseJobGroup());
        assertEquals("groupName", jobPropertyFalse.getJobGroupName());

        // Create a JobInclusionJobProperty with null jobGroupName
        JobInclusionJobProperty jobPropertyNull = new JobInclusionJobProperty(true, null);
        assertTrue(jobPropertyNull.isUseJobGroup());
        assertNull(jobPropertyNull.getJobGroupName());
    }

    @Test
    public void isUseJobGroupTest() {
        // Create a JobInclusionJobProperty with useJobGroup set to true
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, "groupName1");
        assertTrue(jobProperty.isUseJobGroup());
        assertEquals("groupName1", jobProperty.getJobGroupName());

        // Create a JobInclusionJobProperty with useJobGroup set to false
        JobInclusionJobProperty jobPropertyFalse = new JobInclusionJobProperty(false, "groupName2");
        assertFalse(jobPropertyFalse.isUseJobGroup());
        assertEquals("groupName2", jobPropertyFalse.getJobGroupName());

        // Create a JobInclusionJobProperty with null jobGroupName
        JobInclusionJobProperty jobPropertyNull = new JobInclusionJobProperty(true, null);
        assertTrue(jobPropertyNull.isUseJobGroup());
        assertNull(jobPropertyNull.getJobGroupName());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenJobGroupNameNotSet() {
        // Create a JobInclusionJobProperty with useJobGroup set to true and jobGroupName set to null
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, null);
        assertTrue(jobProperty.isUseJobGroup());
        assertNull(jobProperty.getJobGroupName());

        // Create a JobInclusionJobProperty with useJobGroup set to false and jobGroupName set to null
        JobInclusionJobProperty jobPropertyFalse = new JobInclusionJobProperty(false, null);
        assertFalse(jobPropertyFalse.isUseJobGroup());
        assertNull(jobPropertyFalse.getJobGroupName());

        // Create a JobInclusionJobProperty with useJobGroup set to true and jobGroupName set to a non-null value
        JobInclusionJobProperty jobPropertyWithGroupName = new JobInclusionJobProperty(true, "testJobGroupName");
        assertTrue(jobPropertyWithGroupName.isUseJobGroup());
        assertEquals("testJobGroupName", jobPropertyWithGroupName.getJobGroupName());
    }

    @Test
    public void descriptorImplGetDisplayName() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertEquals("XXX", descriptor.getDisplayName());
    }

    @Test
    public void getDescriptorTest() {
        JobInclusionJobProperty jobProperty = new JobInclusionJobProperty(true, null);
        JobInclusionJobProperty.DescriptorImpl descriptor = jobProperty.getDescriptor();

        // Verify that the descriptor is not null
        assertNotNull(descriptor);

        // Verify the display name of the descriptor
        assertEquals("XXX", descriptor.getDisplayName());

        // Verify the type of the descriptor
        assertTrue(descriptor instanceof JobInclusionJobProperty.DescriptorImpl);

        // Verify the isUsed method of the descriptor
        assertFalse(descriptor.isUsed());
    }

    @Test
    public void descriptorImplIsUsed() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertFalse(descriptor.isUsed());
    }

    @Test
    public void getJobGroupsTest() {
        JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        ListBoxModel jobGroups = descriptor.getJobGroups();

        jobGroups.add("testJobGroup", "testGroup");
        // Verify that the jobGroups is not null
        assertNotNull(jobGroups);

        // Verify that the jobGroups is not empty
        assertFalse(jobGroups.isEmpty());

        // Verify the size of the jobGroups
        assertEquals("Expected number of job groups", 1, jobGroups.size());

        // Verify the properties of the first job group
        ListBoxModel.Option firstOption = jobGroups.get(0);
        assertNotNull(firstOption);
        assertEquals("Expected display name", "testJobGroup", firstOption.name);
        assertEquals("Expected value", "testGroup", firstOption.value);
    }
}
