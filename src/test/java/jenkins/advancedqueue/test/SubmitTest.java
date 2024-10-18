package jenkins.advancedqueue.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy;
import jenkins.advancedqueue.priority.strategy.UserIdCauseStrategy;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.stapler.MockStaplerRequest;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

public class SubmitTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @LocalData
    public void shouldGeneratePriorityConfigurationUsingDataBoundConstructor() throws IOException, ServletException {
        PriorityConfiguration priorityConfiguration =
                (PriorityConfiguration) j.jenkins.getDescriptor(PriorityConfiguration.class);
        StaplerResponse2 staplerResponse = mock(StaplerResponse2.class);
        StaplerRequest2 staplerRequest = new MockStaplerRequest.MockStaplerRequestBuilder(
                        j, "advanced-build-queue/priorityConfigSubmit")
                .build();

        String object = loadJson("SubmitTest/priorityConfigSubmitPayload.json");
        when(staplerRequest.getParameter("json")).thenReturn(object);

        List<JobGroup> jobGroupList = priorityConfiguration.getJobGroups();
        assertEquals(0, jobGroupList.size());

        priorityConfiguration.doPriorityConfigSubmit(staplerRequest, staplerResponse);
        jobGroupList = priorityConfiguration.getJobGroups();

        assertEquals(3, jobGroupList.size());
        assertEquals("high", jobGroupList.get(0).getDescription());
        assertEquals(1, jobGroupList.get(0).getPriority());
        assertEquals("medium", jobGroupList.get(1).getDescription());
        assertEquals(2, jobGroupList.get(1).getPriority());
        assertInstanceOf(
                ViewBasedJobInclusionStrategy.class, jobGroupList.get(1).getJobGroupStrategy());
        assertEquals(
                ".*medium",
                ((ViewBasedJobInclusionStrategy) jobGroupList.get(1).getJobGroupStrategy()).getJobPattern());
        assertEquals("special", jobGroupList.get(2).getDescription());
        assertEquals(-1, jobGroupList.get(2).getPriority());
        assertEquals(1, jobGroupList.get(2).getPriorityStrategies().size());
        assertInstanceOf(
                UserIdCauseStrategy.class,
                jobGroupList.get(2).getPriorityStrategies().get(0).getPriorityStrategy());
    }

    private String loadJson(String file) throws IOException {
        InputStream is = SubmitTest.class.getResourceAsStream(file);
        assert is != null;
        return IOUtils.toString(is, Charsets.UTF_8);
    }
}
