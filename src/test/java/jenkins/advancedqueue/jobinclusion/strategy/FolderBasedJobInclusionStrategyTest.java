package jenkins.advancedqueue.jobinclusion.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FolderBasedJobInclusionStrategyTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FolderBasedJobInclusionStrategy strategy;
    private static DecisionLogger decisionLogger;
    private static List<String> loggedMessages;

    @BeforeClass
    public static void createStrategy() throws Exception {
        strategy = new FolderBasedJobInclusionStrategy("testFolder");
    }

    @BeforeClass
    public static void createDecisionLogger() throws Exception {
        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                loggedMessages.add(log);
                return this;
            }
        };
    }

    @Before
    public void clearLoggedMessages() throws Exception {
        loggedMessages = new ArrayList<>();
    }

    @After
    public void checkLoggedMessages() throws Exception {
        assertThat(loggedMessages, is(empty()));
    }

    @Test
    public void getDescriptor() {
        assertNotNull(strategy.getDescriptor());
        assertTrue(
                strategy.getDescriptor()
                        instanceof FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor);
    }

    @Test
    public void all() {
        assertFalse(FolderBasedJobInclusionStrategy.all().isEmpty());
    }

    @Test
    public void getFolderName() {
        assertEquals("testFolder", strategy.getFolderName());
    }

    @Test
    public void contains() throws Exception {
        FreeStyleProject project = this.j.createFreeStyleProject("testFolder_jobName");

        assertTrue(strategy.contains(decisionLogger, project));
    }

    @Test
    public void containsReturnsFalseForJobNotInFolder() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("otherFolder_jobName");

        assertFalse(strategy.contains(decisionLogger, project));
    }

    @Test
    public void containsReturnsTrueForJobInSubFolder() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("testFolder_subFolder_jobName");

        assertTrue(strategy.contains(decisionLogger, project));
    }

    @Test
    public void getListFolderItemsReturnsNonNullListBoxModel() {
        FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor descriptor =
                new FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor();
        ListBoxModel items = descriptor.getListFolderItems();
        assertNotNull(items);
    }

    @Test
    public void getListFolderItemsReturnsCorrectFolderNames() throws Exception {

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
