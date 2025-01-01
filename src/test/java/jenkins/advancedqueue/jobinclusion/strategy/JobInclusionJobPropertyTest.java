package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
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

    private JobInclusionJobProperty jobProperty;
    private FreeStyleProject jobProject;
    private JobInclusionJobProperty.DescriptorImpl descriptor;

    @Before
    public void setUp() throws Exception {
        jobProperty = new JobInclusionJobProperty(true, "TestJobGroup");
        descriptor = jobProperty.getDescriptor();
        jobProject = j.createFreeStyleProject("testFolder_" + testName.getMethodName());
    }

    @After
    public void deleteProject() throws Exception {
        jobProject.delete();
    }

    @Test
    public void getJobGroupNameTest() {
        assertEquals("TestJobGroup", jobProperty.getJobGroupName());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenNotSetAndFalse() {
        // Create a JobInclusionJobProperty with useJobGroup set to false and jobGroupName set to null
        jobProperty = new JobInclusionJobProperty(false, null);
        assertFalse(jobProperty.isUseJobGroup());
        assertNull(jobProperty.getJobGroupName());
    }

    @Test
    public void getJobGroupNameReturnsNullWhenNotSetAndTrue() {
        // Create a JobInclusionJobProperty with useJobGroup set to true and jobGroupName set to null
        jobProperty = new JobInclusionJobProperty(true, null);
        assertTrue(jobProperty.isUseJobGroup());
        assertNull(jobProperty.getJobGroupName());
    }

    @Test
    public void isUseJobGroupReturnsCorrectValue() {
        // Create a JobInclusionJobProperty with useJobGroup set to true
        jobProperty = new JobInclusionJobProperty(true, "groupName");
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
        assertTrue(jobProperty.isUseJobGroup());

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
    public void getDisplayNameTest() {
        assertEquals("XXX", descriptor.getDisplayName());
    }

    @Test
    public void getJobGroupsTest() {
        assertNotNull(descriptor.getJobGroups());
    }

    @Test
    public void isUsedTest() {
        assertFalse(descriptor.isUsed());
    }
}
