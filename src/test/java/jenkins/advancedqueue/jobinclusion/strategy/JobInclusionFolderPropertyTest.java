package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.Run;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JobInclusionFolderPropertyTest {

    private static JenkinsRule j;

    private static FreeStyleProject project;
    private static JobInclusionFolderProperty property;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        project = j.createFreeStyleProject();
        Run r = project.scheduleBuild2(0).get(); // Schedule a build to ensure the queue item is created
        j.assertBuildStatusSuccess(r);
        property = new JobInclusionFolderProperty(true, "testGroup");
    }

    @Test
    void testGetJobGroupName() {
        assertEquals("testGroup", property.getJobGroupName());
    }

    @Test
    void testIsUseJobGroup() {
        assertTrue(property.isUseJobGroup());
    }

    @Test
    void testGetDescriptor() {
        assertThat(property.getDescriptor().getId(), is(JobInclusionFolderProperty.class.getName()));
    }

    @Test
    void testDescriptorImpl() {
        JobInclusionFolderProperty.DescriptorImpl descriptor = new JobInclusionFolderProperty.DescriptorImpl();
        assertThat(descriptor.getDisplayName(), is("XXX"));
        assertThat(descriptor.getJobGroups(), is(empty()));
        assertFalse(descriptor.isUsed());
    }

    @Test
    void testAllJobsJobInclusionStrategy() {
        AllJobsJobInclusionStrategy strategy = new AllJobsJobInclusionStrategy();
        assertTrue(strategy.contains(null, project));
    }
}
