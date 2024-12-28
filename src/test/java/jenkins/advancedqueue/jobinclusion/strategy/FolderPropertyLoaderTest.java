package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FolderPropertyLoaderTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static Folder folder;
    private static FreeStyleProject project;
    private DecisionLogger decisionLogger;
    private List<String> loggedMessages;

    @BeforeClass
    public static void createJob() throws Exception {
        folder = j.createProject(com.cloudbees.hudson.plugins.folder.Folder.class, "testFolder");
        project = folder.createProject(FreeStyleProject.class, "testProject");
    }

    @Before
    public void createDecisionLogger() throws Exception {
        loggedMessages = new ArrayList<>();
        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                loggedMessages.add(log);
                return this;
            }
        };
    }

    @Test
    public void getJobGroupName_returnsGroupName_whenJobGroupIsEnabled() throws Exception {
        JobInclusionFolderProperty property = new JobInclusionFolderProperty(true, "TestGroup");
        folder.getProperties().add(property);

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, project);

        assertEquals("TestGroup", result);
        assertThat(loggedMessages, hasItem("JobGroup is enabled, with JobGroup [TestGroup] ..."));
    }

    @Test
    public void getJobGroupName_returnsNull_whenNoJobGroupProperty() throws Exception {
        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, project);

        assertNull(result);
        assertThat(loggedMessages, hasItem("No match ..."));
    }

    @Test
    public void getJobGroupName_returnsNull_whenJobGroupIsDisabled() throws Exception {
        JobInclusionFolderProperty property = new JobInclusionFolderProperty(false, "TestGroup");
        folder.getProperties().add(property);

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, project);

        assertNull(result);
        assertThat(loggedMessages, hasItem("No match ..."));
    }

    @Test
    public void getJobGroupName_returnsNull_whenParentIsNotFolder() throws Exception {
        FreeStyleProject standaloneProject = j.createFreeStyleProject("standaloneProject");

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, standaloneProject);

        assertNull(result);
        assertThat(loggedMessages, hasItem("No match ..."));
    }
}
