package jenkins.advancedqueue.sorter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SorterStrategyDescriptorTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private FreeStyleProject project;
    private FreeStyleBuild build;
    private SorterStrategyDescriptor descriptor;

    @Before
    public void setUp() throws Exception {
        project = j.createFreeStyleProject();
        build = project.scheduleBuild2(0).get();
        descriptor = new TestSorterStrategy().getDescriptor();
        j.assertBuildStatusSuccess(build);
    }

    @Test
    public void getShortNameReturnsCorrectValue() {
        assertEquals("testShortName", descriptor.getShortName());
    }

    @Test
    public void getKeyReturnsShortName() {
        assertEquals("testShortName", descriptor.getKey());
    }

    @Test
    public void getDescriptorReturnsNonNull() {
        assertNotNull("Descriptor should not be null", descriptor);
    }

    private static class TestSorterStrategy extends SorterStrategy {
        @Override
        public SorterStrategyCallback onNewItem(@NotNull Queue.Item item, SorterStrategyCallback weightCallback) {
            return weightCallback;
        }

        @Override
        public int getNumberOfPriorities() {
            return 1;
        }

        @Override
        public int getDefaultPriority() {
            return 1;
        }

        @Extension
        public static class DescriptorImpl extends TestSorterStrategyDescriptor {}
    }

    private static class TestSorterStrategyDescriptor extends SorterStrategyDescriptor {
        @Override
        public String getShortName() {
            return "testShortName";
        }
    }
}
