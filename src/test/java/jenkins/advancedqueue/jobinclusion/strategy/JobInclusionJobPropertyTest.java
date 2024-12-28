package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionJobPropertyTest {
    private JobInclusionJobProperty jobProperty;
    private JobInclusionJobProperty.DescriptorImpl descriptor;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setup() {
        jobProperty = new JobInclusionJobProperty(true, "TestJobGroup");
        descriptor = new JobInclusionJobProperty.DescriptorImpl();
    }

    @Test
    public void getJobGroupNameTest() {
        assertEquals("TestJobGroup", jobProperty.getJobGroupName());
    }

    @Test
    public void isUseJobGroupTest() {
        assertTrue(jobProperty.isUseJobGroup());
    }

    @Test
    public void getDescriptorTest() {
        assertNotNull(jobProperty.getDescriptor());
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
