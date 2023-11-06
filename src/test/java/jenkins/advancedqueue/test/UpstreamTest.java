package jenkins.advancedqueue.test;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jenkins.advancedqueue.testutil.ExpectedItem;
import jenkins.advancedqueue.testutil.JobHelper;
import jenkins.advancedqueue.testutil.TestRunListener;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import jenkins.advancedqueue.test.BuildUpstreamCause;

public class UpstreamTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private final JobHelper jobHelper = new JobHelper(j);

    @Test
    @LocalData
    public void testOrphanDownstreamJob() throws Exception {
        // Job 0 should run with default priority, as upstream build is unknown
        TestRunListener.init(new ExpectedItem("Job 0", 5));
        jobHelper.scheduleProjects(createUpstreamCause(UpstreamCause.class, "Job X", 987)).go();
        j.waitUntilNoActivity();

        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    public void testUserJobAndAssociatedDownstreamJob() throws Exception {
        // Upstream job should run with high priority (user triggered)
        TestRunListener.init(new ExpectedItem("Upstream", 1));
        jobHelper.scheduleProject("Upstream", new UserIdCause()).go();
        j.waitUntilNoActivity();

        // Downstream job 1 should run with priority of upstream job build 1
        TestRunListener.init(new ExpectedItem("Downstream1", 1));
        jobHelper
                .scheduleProject("Downstream1", createUpstreamCause(UpstreamCause.class, "Upstream", 1))
                .go();
        j.waitUntilNoActivity();

        // Downstream job 2 should run with priority of upstream job build 2 (not present, i.e. default priority
        // should be used)
        TestRunListener.init(new ExpectedItem("Downstream2", 5));
        jobHelper
                .scheduleProject("Downstream2", createUpstreamCause(UpstreamCause.class, "Upstream", 2))
                .go();
        j.waitUntilNoActivity();

        TestRunListener.assertStartedItems();
    }

    @Test
    @LocalData
    public void testExtendedUpstreamCause() throws Exception {
        // Upstream job should run with high priority (user triggered)
        TestRunListener.init(new ExpectedItem("Upstream", 1));
        jobHelper.scheduleProject("Upstream", new UserIdCause()).go();
        j.waitUntilNoActivity();

        // Downstream job 1 should run with priority of upstream job build 1
        TestRunListener.init(new ExpectedItem("Downstream1", 1));
        jobHelper
                .scheduleProject("Downstream1", createUpstreamCause(BuildUpstreamCause.class, "Upstream", 1))
                .go();
        j.waitUntilNoActivity();

        // Downstream job 2 should run with priority of upstream job build 2 (not present, i.e. default priority
        // should be used)
        TestRunListener.init(new ExpectedItem("Downstream2", 5));
        jobHelper
                .scheduleProject("Downstream2", createUpstreamCause(BuildUpstreamCause.class, "Upstream", 2))
                .go();
        j.waitUntilNoActivity();

        TestRunListener.assertStartedItems();
    }

    @CheckForNull
    private <T extends UpstreamCause> T createUpstreamCause(Class<T> clazz, final String upstreamProject, final int upstreamBuild) throws Exception {
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (final Constructor<?> cons : constructors) {
            if (Arrays.equals(
                    cons.getParameterTypes(), new Class<?>[] {String.class, int.class, String.class, List.class})) {
                cons.setAccessible(true);
                return clazz.cast(
                        cons.newInstance(upstreamProject, upstreamBuild, "url", Collections.<Cause>emptyList()));
            }
        }
        return null;
    }
}
