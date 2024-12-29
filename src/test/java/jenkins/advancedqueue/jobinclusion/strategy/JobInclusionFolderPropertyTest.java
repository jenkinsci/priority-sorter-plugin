package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionFolderPropertyTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private JobInclusionFolderProperty property;
    private JobInclusionFolderProperty.DescriptorImpl descriptor;

    @Before
    public void setup() {
        property = new JobInclusionFolderProperty(true, "TestJobGroup");
        descriptor = new JobInclusionFolderProperty.DescriptorImpl();
    }

    @Test
    public void getJobGroupNameTest() {
        assertEquals("TestJobGroup", property.getJobGroupName());
    }

    @Test
    public void isUseJobGroupTest() {
        assertTrue(property.isUseJobGroup());
    }

    @Test
    public void getDescriptorTest() {
        assertNotNull(property.getDescriptor());
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
