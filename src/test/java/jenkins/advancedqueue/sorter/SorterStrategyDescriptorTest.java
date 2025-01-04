package jenkins.advancedqueue.sorter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SorterStrategyDescriptorTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static FreeStyleProject project;
    private static FreeStyleBuild build;
    private static SorterStrategyDescriptor descriptor;

    private static final String STRATEGY_NAME = "strategy short name";

    @BeforeClass
    public static void runJob() throws Exception {
        project = j.createFreeStyleProject();
        build = project.scheduleBuild2(0).get();
        descriptor = new TestSorterStrategy().getDescriptor();
        j.assertBuildStatusSuccess(build);
    }

    @Test
    public void getShortNameReturnsCorrectValue() {
        assertEquals(STRATEGY_NAME, descriptor.getShortName());
    }

    @Test
    public void getKeyReturnsShortName() {
        assertEquals(STRATEGY_NAME, descriptor.getKey());
    }

    @Test
    public void getDescriptorReturnsNonNull() {
        assertNotNull("Descriptor should not be null", descriptor);
    }

    private static class TestSorterStrategy extends SorterStrategy {
        @Override
        public SorterStrategyCallback onNewItem(@NonNull Queue.Item item, SorterStrategyCallback weightCallback) {
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
            return STRATEGY_NAME;
        }
    }
}
