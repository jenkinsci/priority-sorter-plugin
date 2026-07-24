package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JobInclusionJobPropertyTest {

    private static JenkinsRule j;

    private JobInclusionJobProperty jobProperty;
    private FreeStyleProject jobProject;
    private JobInclusionJobProperty.DescriptorImpl descriptor;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
    }

    @BeforeEach
    void beforeEach(TestInfo info) throws Exception {
        jobProperty = new JobInclusionJobProperty(true, "TestJobGroup");
        descriptor = jobProperty.getDescriptor();
        jobProject = j.createFreeStyleProject(
                "testFolder_" + info.getTestMethod().orElseThrow().getName());
    }

    @AfterEach
    void afterEach() throws Exception {
        jobProject.delete();
    }

    @Test
    void getJobGroupNameTest() {
        assertEquals("TestJobGroup", jobProperty.getJobGroupName());
    }

    @Test
    void getJobGroupNameReturnsNullWhenNotSetAndFalse() {
        // Create a JobInclusionJobProperty with useJobGroup set to false and jobGroupName set to null
        jobProperty = new JobInclusionJobProperty(false, null);
        assertFalse(jobProperty.isUseJobGroup());
        assertNull(jobProperty.getJobGroupName());
    }

    @Test
    void getJobGroupNameReturnsNullWhenNotSetAndTrue() {
        // Create a JobInclusionJobProperty with useJobGroup set to true and jobGroupName set to null
        jobProperty = new JobInclusionJobProperty(true, null);
        assertTrue(jobProperty.isUseJobGroup());
        assertNull(jobProperty.getJobGroupName());
    }

    @Test
    void isUseJobGroupReturnsCorrectValue() {
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
    void isUseJobGroupTest() {
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
    void getJobGroupNameReturnsNullWhenJobGroupNameNotSet() {
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
    void getDisplayNameTest() {
        assertEquals("Priority Sorter Job Group", descriptor.getDisplayName());
    }

    @Test
    void getJobGroupsTest() {
        assertNotNull(descriptor.getJobGroups());
    }

    @Test
    void isUsedTest() {
        assertFalse(descriptor.isUsed());
    }
}
