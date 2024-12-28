package jenkins.advancedqueue.jobinclusion.strategy;

import static org.junit.Assert.*;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.DecisionLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FolderBasedJobInclusionStrategyTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private FolderBasedJobInclusionStrategy strategy;
    private static DecisionLogger decisionLogger;

    @Before
    public void setUp() throws Exception {
        strategy = new FolderBasedJobInclusionStrategy("testFolder");
        decisionLogger = new DecisionLogger() {
            @Override
            public DecisionLogger addDecisionLog(int indent, String log) {
                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {}

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
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("testFolder_jobName");

        assertTrue(strategy.contains(decisionLogger, project));
    }

    @Test
    public void containsReturnsFalseForJobNotInFolder() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("otherFolder_jobName");

        assertFalse(strategy.contains(decisionLogger, project));
    }

    @Test
    public void containsReturnsTrueForJobInSubFolder() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("testFolder_subFolder_jobName");

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

        Folder folder1 = jenkinsRule.jenkins.createProject(Folder.class, "folder1");
        Folder folder2 = jenkinsRule.jenkins.createProject(Folder.class, "folder2");

        FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor descriptor =
                new FolderBasedJobInclusionStrategy.FolderBasedJobInclusionStrategyDescriptor();
        ListBoxModel items = descriptor.getListFolderItems();

        assertEquals(2, items.size());
        assertEquals("folder1", items.get(0).name);
        assertEquals("folder2", items.get(1).name);
    }
}
