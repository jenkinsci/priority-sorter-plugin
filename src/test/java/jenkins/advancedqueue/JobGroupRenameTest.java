package jenkins.advancedqueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import jakarta.servlet.ServletException;
import java.util.List;
import jenkins.advancedqueue.jobinclusion.strategy.JobInclusionFolderProperty;
import jenkins.advancedqueue.jobinclusion.strategy.JobInclusionJobProperty;
import jenkins.advancedqueue.jobinclusion.strategy.PropertyBasedJobInclusionStrategy;
import org.htmlunit.html.HtmlHiddenInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.MockStaplerRequest;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

@WithJenkins
class JobGroupRenameTest {
    @Test
    void renamingAGroupUpdatesJobsAndFolders(JenkinsRule jenkins) throws Exception {
        FreeStyleProject job = jenkins.createFreeStyleProject("member");
        job.addProperty(new JobInclusionJobProperty(true, "original"));
        FreeStyleProject unrelated = jenkins.createFreeStyleProject("unrelated");
        unrelated.addProperty(new JobInclusionJobProperty(true, "other"));
        Folder folder = jenkins.jenkins.createProject(Folder.class, "member-folder");
        folder.addProperty(new JobInclusionFolderProperty(true, "original"));

        PriorityConfiguration configuration = PriorityConfiguration.get();
        JobGroup group = group("original");
        configuration.setJobGroups(List.of(group));
        String persistentId = group.getPersistentId();

        HtmlPage page = jenkins.createWebClient().goTo("advanced-build-queue/");
        HtmlHiddenInput identity = page.getFirstByXPath("//input[@name='persistentId']");
        assertNotNull(identity);
        assertEquals(persistentId, identity.getValue());

        submit(jenkins, configuration, persistentId, "renamed");

        assertEquals(
                group.getPersistentId(), configuration.getJobGroups().get(0).getPersistentId());
        assertEquals("renamed", job.getProperty(JobInclusionJobProperty.class).getJobGroupName());
        assertEquals(
                "other", unrelated.getProperty(JobInclusionJobProperty.class).getJobGroupName());
        JobInclusionFolderProperty folderProperty = folder.getProperties().get(JobInclusionFolderProperty.class);
        assertNotNull(folderProperty);
        assertEquals("renamed", folderProperty.getJobGroupName());
        assertEquals(
                "renamed",
                ((PropertyBasedJobInclusionStrategy)
                                configuration.getJobGroups().get(0).getJobGroupStrategy())
                        .getName());
        assertTrue(configuration
                .getJobGroups()
                .get(0)
                .getJobGroupStrategy()
                .contains(
                        new DecisionLogger() {
                            @Override
                            public DecisionLogger addDecisionLog(int indent, String message) {
                                return this;
                            }
                        },
                        job));

        jenkins.jenkins.reload();
        FreeStyleProject reloadedJob = jenkins.jenkins.getItemByFullName("member", FreeStyleProject.class);
        Folder reloadedFolder = jenkins.jenkins.getItemByFullName("member-folder", Folder.class);
        assertNotNull(reloadedJob);
        assertNotNull(reloadedFolder);
        assertEquals(
                "renamed",
                reloadedJob.getProperty(JobInclusionJobProperty.class).getJobGroupName());
        assertEquals(
                "renamed",
                reloadedFolder
                        .getProperties()
                        .get(JobInclusionFolderProperty.class)
                        .getJobGroupName());

        PriorityConfiguration reloadedConfiguration = PriorityConfiguration.get();
        assertEquals(persistentId, reloadedConfiguration.getJobGroups().get(0).getPersistentId());
        submit(jenkins, reloadedConfiguration, persistentId, "renamed-again");
        assertEquals(
                "renamed-again",
                reloadedJob.getProperty(JobInclusionJobProperty.class).getJobGroupName());
        assertEquals(
                "renamed-again",
                reloadedFolder
                        .getProperties()
                        .get(JobInclusionFolderProperty.class)
                        .getJobGroupName());
    }

    @Test
    void reorderingGroupsDoesNotChangeMembership(JenkinsRule jenkins) throws Exception {
        FreeStyleProject job = jenkins.createFreeStyleProject("member");
        job.addProperty(new JobInclusionJobProperty(true, "first"));
        JobGroup first = group("first");
        JobGroup second = group("second");
        PriorityConfiguration configuration = PriorityConfiguration.get();
        configuration.setJobGroups(List.of(first, second));

        String json = "{" + "\"jobGroup\":[" + groupJson(second.getPersistentId(), "second") + ","
                + groupJson(first.getPersistentId(), "first") + "]}";
        submitJson(jenkins, configuration, json);

        assertEquals(
                second.getPersistentId(), configuration.getJobGroups().get(0).getPersistentId());
        assertEquals(
                first.getPersistentId(), configuration.getJobGroups().get(1).getPersistentId());
        assertEquals("first", job.getProperty(JobInclusionJobProperty.class).getJobGroupName());
    }

    @Test
    void ambiguousGroupNameDoesNotChangeJobs(JenkinsRule jenkins) throws Exception {
        FreeStyleProject job = jenkins.createFreeStyleProject("member");
        job.addProperty(new JobInclusionJobProperty(true, "shared"));
        JobGroup first = group("shared");
        JobGroup second = group("shared");
        PriorityConfiguration configuration = PriorityConfiguration.get();
        configuration.setJobGroups(List.of(first, second));

        String json = "{" + "\"jobGroup\":[" + groupJson(first.getPersistentId(), "renamed") + ","
                + groupJson(second.getPersistentId(), "shared") + "]}";
        assertThrows(ServletException.class, () -> submitJson(jenkins, configuration, json));

        assertEquals("shared", job.getProperty(JobInclusionJobProperty.class).getJobGroupName());
        assertEquals(
                "shared",
                ((PropertyBasedJobInclusionStrategy)
                                configuration.getJobGroups().get(0).getJobGroupStrategy())
                        .getName());
    }

    private JobGroup group(String name) {
        JobGroup group = new JobGroup();
        group.setJobGroupStrategy(new PropertyBasedJobInclusionStrategy(name));
        return group;
    }

    private void submit(JenkinsRule jenkins, PriorityConfiguration configuration, String persistentId, String name)
            throws Exception {
        submitJson(jenkins, configuration, "{\"jobGroup\":[" + groupJson(persistentId, name) + "]}");
    }

    private String groupJson(String persistentId, String name) {
        return """
                {"persistentId":"%s","jobGroupStrategy":{
                  "name":"%s",
                  "stapler-class":"jenkins.advancedqueue.jobinclusion.strategy.PropertyBasedJobInclusionStrategy",
                  "$class":"jenkins.advancedqueue.jobinclusion.strategy.PropertyBasedJobInclusionStrategy"
                },"priority":"2"}
                """.formatted(persistentId, name);
    }

    private void submitJson(JenkinsRule jenkins, PriorityConfiguration configuration, String json) throws Exception {
        StaplerRequest2 request = new MockStaplerRequest.MockStaplerRequestBuilder(
                        jenkins, "advanced-build-queue/priorityConfigSubmit")
                .build();
        StaplerResponse2 response = mock(StaplerResponse2.class);
        when(request.getParameter("json")).thenReturn(json);
        configuration.doPriorityConfigSubmit(request, response);
    }
}
