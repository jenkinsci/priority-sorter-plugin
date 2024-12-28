package jenkins.advancedqueue.jobinclusion.strategy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

public class JobInclusionJobPropertyTest{
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
    public void getJobGroupNameTest(){
        assertEquals("TestJobGroup", jobProperty.getJobGroupName());
    }

    @Test
    public void isUseJobGroupTest(){
        assertTrue(jobProperty.isUseJobGroup());
    }

    @Test
    public void getDescriptorTest(){
       //JobInclusionJobProperty.DescriptorImpl descriptor = mock(JobInclusionJobProperty.DescriptorImpl.class);
       //when(descriptor.getDisplayName()).thenReturn("Mock Descriptor");
       assertNotNull(jobProperty.getDescriptor());
    }

    @Test
    public void getDisplayNameTest(){
        //JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertEquals("XXX", descriptor.getDisplayName());
    }

    @Test
    public void getJobGroupsTest(){
        //JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        assertNotNull(descriptor.getJobGroups());
    }

    @Test
    public void isUsedTest(){
        //JobInclusionJobProperty.DescriptorImpl descriptor = new JobInclusionJobProperty.DescriptorImpl();
        //System.out.println("hehe");
        //System.out.println(descriptor.isUsed());
        assertFalse(descriptor.isUsed());
    }

}