package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.*;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FolderPropertyLoaderTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private Folder folder;
    private FreeStyleProject j;
    private DecisionLogger decisionLogger;

    @Before
    public void setUp() throws Exception {
        folder = jenkinsRule.createProject(com.cloudbees.hudson.plugins.folder.Folder.class, "testFolder");
        j = folder.createProject(FreeStyleProject.class, "testProject");
        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                return null;
            }
        };
    }

    @Test
    public void getJobGroupName_returnsGroupName_whenJobGroupIsEnabled() throws Exception {
        JobInclusionFolderProperty property = new JobInclusionFolderProperty(true, "TestGroup");
        folder.getProperties().add(property);

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, j);

        assertEquals("TestGroup", result);
    }

    @Test
    public void getJobGroupName_returnsNull_whenNoJobGroupProperty() throws Exception {
        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, j);

        assertNull(result);
    }

    @Test
    public void getJobGroupName_returnsNull_whenJobGroupIsDisabled() throws Exception {
        JobInclusionFolderProperty property = new JobInclusionFolderProperty(false, "TestGroup");
        folder.getProperties().add(property);

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, j);

        assertNull(result);
    }

    @Test
    public void getJobGroupName_returnsNull_whenParentIsNotFolder() throws Exception {
        FreeStyleProject standaloneProject = jenkinsRule.createFreeStyleProject("standaloneProject");

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, standaloneProject);

        assertNull(result);
    }
}
