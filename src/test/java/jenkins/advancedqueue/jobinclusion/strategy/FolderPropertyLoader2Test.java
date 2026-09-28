package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Surefire 3.6.0 changed the order of execution of unit tests.  That
 * exposed an order dependency in the tests, so these tests were split
 * to a separate source file.  That was simpler than identifying the
 * cause of the order dependency in the tests.
 */
@WithJenkins
class FolderPropertyLoader2Test {

    private static JenkinsRule j;

    private static Folder folder;
    private static FreeStyleProject project;
    private DecisionLogger decisionLogger;
    private List<String> loggedMessages;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        folder = j.createProject(com.cloudbees.hudson.plugins.folder.Folder.class, "testFolder");
        project = folder.createProject(FreeStyleProject.class, "testProject");
    }

    @BeforeEach
    void beforeEach() throws Exception {
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
    void getJobGroupName_returnsNull_whenNoJobGroupProperty() throws Exception {
        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, project);

        assertNull(result);
        assertThat(loggedMessages, hasItem("No match ..."));
    }

    @Test
    void getJobGroupName_returnsNull_whenJobGroupIsDisabled() throws Exception {
        JobInclusionFolderProperty property = new JobInclusionFolderProperty(false, "TestGroup");
        folder.getProperties().add(property);

        String result = FolderPropertyLoader.getJobGroupName(decisionLogger, project);

        assertNull(result);
        assertThat(loggedMessages, hasItem("No match ..."));
    }
}
