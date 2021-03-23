package jenkins.advancedqueue.test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;

import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.jobinclusion.strategy.FolderBasedJobInclusionStrategy;
import jenkins.advancedqueue.jobinclusion.strategy.AllJobsJobInclusionStrategy;

public class ConfigurationAsCodeTest {

	@Rule public JenkinsRule r = new JenkinsRule();

	@Test
	public void PrioritySorterConfiguration() throws ConfiguratorException {
		ConfigurationAsCode.get().configure(ConfigurationAsCodeTest.class.getResource("/jcasc/PrioritySorterConfiguration.yaml").toString());
		PrioritySorterConfiguration prioSorterCfg = PrioritySorterConfiguration.get();
		assertThat(prioSorterCfg.getOnlyAdminsMayEditPriorityConfiguration(), is(true));
		assertThat(prioSorterCfg.getStrategy().getDefaultPriority(), is(3));
		assertThat(prioSorterCfg.getStrategy().getNumberOfPriorities(), is(5));
	}

	@Test
	public void PriorityConfiguration() throws ConfiguratorException {
		ConfigurationAsCode.get().configure(ConfigurationAsCodeTest.class.getResource("/jcasc/PriorityConfiguration.yaml").toString());
		PriorityConfiguration prioCfg = PriorityConfiguration.get();
		assertThat(prioCfg.getJobGroups().size(), is(2));

		JobGroup jobGroupFirst = prioCfg.getJobGroups().get(0);
		assertThat(jobGroupFirst.getId(), is(0));
		assertThat(jobGroupFirst.getDescription(), is("Complex"));
		assertThat(jobGroupFirst.isRunExclusive(), is(true));
		assertThat(jobGroupFirst.isUsePriorityStrategies(), is(true));
		assertThat(jobGroupFirst.getPriorityStrategies().size(), is(6));
		assertThat(jobGroupFirst.getJobGroupStrategy().getClass(), is(FolderBasedJobInclusionStrategy.class));

		FolderBasedJobInclusionStrategy folderBasedStrategy = (FolderBasedJobInclusionStrategy) jobGroupFirst.getJobGroupStrategy();
		assertThat(folderBasedStrategy.getFolderName(), is("Jenkins"));

		JobGroup jobGroupSecond = prioCfg.getJobGroups().get(1);
		assertThat(jobGroupSecond.getId(), is(1));
		assertThat(jobGroupSecond.getDescription(), is("Simple"));
		assertThat(jobGroupSecond.getPriorityStrategies().size(), is(0));
		assertThat(jobGroupSecond.isRunExclusive(), is(false));
		assertThat(jobGroupSecond.isUsePriorityStrategies(), is(false));
		assertThat(jobGroupSecond.getJobGroupStrategy().getClass(), is(AllJobsJobInclusionStrategy.class));
	}
}