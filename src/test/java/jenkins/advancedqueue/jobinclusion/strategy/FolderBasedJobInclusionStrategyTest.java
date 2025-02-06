package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FolderBasedJobInclusionStrategyTest {

    private static JenkinsRule j;

    private static FolderBasedJobInclusionStrategy strategy;
    private static DecisionLogger decisionLogger;
    private static List<String> loggedMessages;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        strategy = new FolderBasedJobInclusionStrategy("testFolder");
        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                loggedMessages.add(log);
                return this;
            }
        };
    }

    @BeforeEach
    void beforeEach() throws Exception {
        loggedMessages = new ArrayList<>();
    }

    @AfterEach
    void afterEach() throws Exception {
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    void getDescriptor() {
        assertNotNull(strategy.getDescriptor());
        assertInstanceOf(
                FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor.class,
                strategy.getDescriptor());
    }

    @Test
    void all() {
        assertFalse(FolderBasedJobInclusionStrategy.all().isEmpty());
    }

    @Test
    void getFolderName() {
        assertEquals("testFolder", strategy.getFolderName());
    }

    @Test
    void contains() throws Exception {
        FreeStyleProject project = this.j.createFreeStyleProject("testFolder_jobName");

        assertTrue(strategy.contains(decisionLogger, project));
    }

    @Test
    void containsReturnsFalseForJobNotInFolder() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("otherFolder_jobName");

        assertFalse(strategy.contains(decisionLogger, project));
    }

    @Test
    void containsReturnsTrueForJobInSubFolder() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("testFolder_subFolder_jobName");

        assertTrue(strategy.contains(decisionLogger, project));
    }

    @Test
    void getListFolderItemsReturnsNonNullListBoxModel() {
        FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor descriptor =
                new FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor();
        ListBoxModel items = descriptor.getListFolderItems();
        assertNotNull(items);
    }

    @Test
    void getListFolderItemsReturnsCorrectFolderNames() throws Exception {

        j.jenkins.createProject(Folder.class, "folder1");
        j.jenkins.createProject(Folder.class, "folder2");

        FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor descriptor =
                new FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor();
        ListBoxModel items = descriptor.getListFolderItems();

        assertEquals(2, items.size());
        assertEquals("folder1", items.get(0).name);
        assertEquals("folder2", items.get(1).name);
    }
}
