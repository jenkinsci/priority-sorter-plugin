package jenkins.advancedqueue.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.casc.model.Scalar;
import io.jenkins.plugins.casc.model.Sequence;
import io.jenkins.plugins.casc.yaml.YamlSource;
import io.jenkins.plugins.casc.yaml.YamlUtils;
import java.util.Collections;
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
import org.jvnet.hudson.test.recipes.LocalData;

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

    @Test
    @LocalData
    public void exportTest() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        Mapping expectedConfig = YamlUtils.loadFrom(
                Collections.singletonList(YamlSource.of(ConfigurationAsCodeTest.class
                        .getResource("ConfigurationAsCodeTest/ExportTest/PriorityConfiguration.yaml")
                        .toString())),
                context);
        expectedConfig = expectedConfig
                .get("unclassified")
                .asMapping()
                .get("priorityConfiguration")
                .asMapping();

        PriorityConfiguration configuration =
                (PriorityConfiguration) r.jenkins.getDescriptor(PriorityConfiguration.class);
        Configurator<PriorityConfiguration> c = context.lookupOrFail(PriorityConfiguration.class);

        CNode node = c.describe(configuration, context);
        assertNotNull(node);
        Mapping exportedConfig = node.asMapping();

        assertEqualsMapping(expectedConfig, exportedConfig);
    }

    private static void assertEqualsMapping(Mapping expectedMapping, Mapping actualMapping)
            throws ConfiguratorException {
        for (String key : expectedMapping.keySet()) {
            CNode expected = expectedMapping.get(key);
            assertTrue(actualMapping.containsKey(key));
            CNode actual = actualMapping.get(key);
            assertEquals(expected.getClass(), actual.getClass());
            if (expected instanceof Mapping) {
                assertEqualsMapping(expected.asMapping(), actual.asMapping());
            } else if (expected instanceof Sequence) {
                assertEqualsSequence(expected.asSequence(), actual.asSequence());
            } else if (expected instanceof Scalar) {
                assertEquals(expected.asScalar().getValue(), actual.asScalar().getValue());
            }
        }
        assertEquals(expectedMapping.keySet().size(), actualMapping.keySet().size());
    }

    private static void assertEqualsSequence(Sequence expectedSequence, Sequence actualSequence)
            throws ConfiguratorException {
        assertEquals(expectedSequence.size(), actualSequence.size());
        for (int i = 0; i < expectedSequence.size(); i++) {
            CNode expected = expectedSequence.get(i);
            CNode actual = actualSequence.get(i);
            if (expected instanceof Mapping) {
                assertEqualsMapping(expected.asMapping(), actual.asMapping());
            } else if (expected instanceof Sequence) {
                assertEqualsSequence(expected.asSequence(), actual.asSequence());
            } else if (expected instanceof Scalar) {
                assertEquals(expected.asScalar().getValue(), actual.asScalar().getValue());
            }
        }
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
