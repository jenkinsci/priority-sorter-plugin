package jenkins.advancedqueue.sorter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SorterStrategyDescriptorTest {

    private static JenkinsRule j;

    private static FreeStyleProject project;
    private static FreeStyleBuild build;
    private static SorterStrategy strategy;
    private static SorterStrategyDescriptor descriptor;

    private static final String STRATEGY_NAME = "strategy short name";
    private static final int NUMBER_OF_PRIORITIES = 9;
    private static final int DEFAULT_PRIORITY = 4;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) throws Exception {
        j = rule;
        project = j.createFreeStyleProject();
        build = project.scheduleBuild2(0).get();
        strategy = new TestSorterStrategy();
        descriptor = strategy.getDescriptor();
        j.assertBuildStatusSuccess(build);
    }

    @Test
    void getNumberOfPriorities() {
        assertEquals(NUMBER_OF_PRIORITIES, strategy.getNumberOfPriorities());
    }

    @Test
    void getDefaultPriority() {
        assertEquals(DEFAULT_PRIORITY, strategy.getDefaultPriority());
    }

    @Test
    void getShortNameReturnsCorrectValue() {
        assertEquals(STRATEGY_NAME, descriptor.getShortName());
    }

    @Test
    void getKeyReturnsShortName() {
        assertEquals(STRATEGY_NAME, descriptor.getKey());
    }

    private static class TestSorterStrategy extends SorterStrategy {
        @Override
        public SorterStrategyCallback onNewItem(@NonNull Queue.Item item, SorterStrategyCallback weightCallback) {
            return weightCallback;
        }

        @Override
        public int getNumberOfPriorities() {
            return NUMBER_OF_PRIORITIES;
        }

        @Override
        public int getDefaultPriority() {
            return DEFAULT_PRIORITY;
        }

        @Extension
        public static class DescriptorImpl extends SorterStrategyDescriptor {
            @Override
            public String getShortName() {
                return STRATEGY_NAME;
            }
        }
    }
}
