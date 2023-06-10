package jenkins.advancedqueue.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.AllJobsJobInclusionStrategy;
import jenkins.advancedqueue.jobinclusion.strategy.FolderBasedJobInclusionStrategy;
import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.advancedqueue.priority.strategy.BuildParameterStrategy;
import jenkins.advancedqueue.priority.strategy.CLICauseStrategy;
import jenkins.advancedqueue.priority.strategy.HealthStrategy;
import jenkins.advancedqueue.priority.strategy.JobPropertyStrategy;
import jenkins.advancedqueue.priority.strategy.UpstreamCauseStrategy;
import jenkins.advancedqueue.priority.strategy.UserIdCauseStrategy;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ConfigurationAsCodeTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void PrioritySorterConfiguration() throws ConfiguratorException {
        ConfigurationAsCode.get()
                .configure(ConfigurationAsCodeTest.class
                        .getResource("ConfigurationAsCodeTest/PrioritySorterConfiguration.yaml")
                        .toString());
        PrioritySorterConfiguration prioSorterCfg = PrioritySorterConfiguration.get();
        assertThat(prioSorterCfg.getOnlyAdminsMayEditPriorityConfiguration(), is(true));
        assertThat(prioSorterCfg.getStrategy().getDefaultPriority(), is(3));
        assertThat(prioSorterCfg.getStrategy().getNumberOfPriorities(), is(5));
    }

    @Test
    public void PriorityConfiguration() throws ConfiguratorException {
        ConfigurationAsCode.get()
                .configure(ConfigurationAsCodeTest.class
                        .getResource("ConfigurationAsCodeTest/PriorityConfiguration.yaml")
                        .toString());
        PriorityConfiguration prioCfg = PriorityConfiguration.get();
        assertThat(prioCfg.getJobGroups().size(), is(2));

        JobGroup jobGroupFirst = prioCfg.getJobGroups().get(0);
        assertThat(jobGroupFirst.getId(), is(0));
        assertThat(jobGroupFirst.getDescription(), is("Complex"));
        assertThat(jobGroupFirst.isRunExclusive(), is(true));
        assertThat(jobGroupFirst.isUsePriorityStrategies(), is(true));
        assertThat(jobGroupFirst.getPriorityStrategies().size(), is(7));

        UserIdCauseStrategy userIdCauseStrategy = assertStrategy(UserIdCauseStrategy.class, jobGroupFirst, 0);
        assertThat(userIdCauseStrategy.getPriority(null), is(1));

        assertStrategy(UpstreamCauseStrategy.class, jobGroupFirst, 1);

        userIdCauseStrategy = assertStrategy(UserIdCauseStrategy.class, jobGroupFirst, 2);
        assertThat(userIdCauseStrategy.getPriority(null), is(3));

        CLICauseStrategy clicauseStrategy = assertStrategy(CLICauseStrategy.class, jobGroupFirst, 3);
        assertThat(clicauseStrategy.getPriority(null), is(4));

        assertStrategy(JobPropertyStrategy.class, jobGroupFirst, 4);

        BuildParameterStrategy buildParameterStrategy = assertStrategy(BuildParameterStrategy.class, jobGroupFirst, 5);
        assertThat(buildParameterStrategy.getParameterName(), is("priority"));

        HealthStrategy healthStrategy = assertStrategy(HealthStrategy.class, jobGroupFirst, 6);
        assertThat(healthStrategy.getPriority(), is(2));
        assertThat(healthStrategy.getHealth(), is("HEALTH_0_TO_20"));
        assertThat(healthStrategy.getSelection(), is("BETTER"));

        assertThat(jobGroupFirst.getJobGroupStrategy().getClass(), is(FolderBasedJobInclusionStrategy.class));

        FolderBasedJobInclusionStrategy folderBasedStrategy =
                (FolderBasedJobInclusionStrategy) jobGroupFirst.getJobGroupStrategy();
        assertThat(folderBasedStrategy.getFolderName(), is("Jenkins"));

        JobGroup jobGroupSecond = prioCfg.getJobGroups().get(1);
        assertThat(jobGroupSecond.getId(), is(1));
        assertThat(jobGroupSecond.getDescription(), is("Simple"));
        assertThat(jobGroupSecond.isRunExclusive(), is(false));
        assertThat(jobGroupSecond.isUsePriorityStrategies(), is(false));
        assertThat(jobGroupSecond.getPriorityStrategies().size(), is(0));
        assertThat(jobGroupSecond.getJobGroupStrategy().getClass(), is(AllJobsJobInclusionStrategy.class));
    }

    private static <T extends PriorityStrategy> T assertStrategy(Class<T> strategyClass, JobGroup jobGroup, int index) {
        assertThat(
                jobGroup.getPriorityStrategies()
                        .get(index)
                        .getPriorityStrategy()
                        .getClass(),
                is(strategyClass));
        try {
            return strategyClass.cast(
                    jobGroup.getPriorityStrategies().get(index).getPriorityStrategy());
        } catch (ClassCastException e) {
            return null;
        }
    }
}
