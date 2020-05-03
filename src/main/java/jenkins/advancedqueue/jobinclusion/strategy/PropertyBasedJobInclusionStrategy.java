/*
 * The MIT License
 *
 * Copyright (c) 2014, Magnus Sandberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.advancedqueue.jobinclusion.strategy;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.util.ListBoxModel;

import java.util.List;

import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
@Extension
public class PropertyBasedJobInclusionStrategy extends JobInclusionStrategy {

	@Extension
	static public class PropertyBasedJobInclusionStrategyDescriptor extends Descriptor<JobInclusionStrategy> {

		private boolean cloudbeesFolders = true;

		@Override
		public String getDisplayName() {
			if (cloudbeesFolders) {
				return "Jobs and Folders marked for inclusion";
			} else {
				return "Jobs marked for inclusion";
			}
		}

		public PropertyBasedJobInclusionStrategyDescriptor() {
			Plugin plugin = Jenkins.get().getPlugin("cloudbees-folder");
			if(plugin == null || !plugin.getWrapper().isEnabled()){
				cloudbeesFolders = false;
			}
		}

	};

	private String name;

	public PropertyBasedJobInclusionStrategy() {}

	@DataBoundConstructor
	public PropertyBasedJobInclusionStrategy(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job) {
		JobInclusionJobProperty property = job.getProperty(JobInclusionJobProperty.class);
		decisionLogger.addDecisionLog(2, "Checking for Job Property inclusion for [" + name + "]...");
		if (property != null && property.isUseJobGroup()) {
			decisionLogger.addDecisionLog(3, "JobGroup is enabled on job, with JobGroup [" + property.getJobGroupName()
					+ "] ...");
			boolean match = name.equals(property.getJobGroupName());
			if (match) {
				decisionLogger.addDecisionLog(3, "Job is included in JobGroup ...");
			} else {
				decisionLogger.addDecisionLog(3, "Job is not included in JobGroup ...");
			}
			return match;
		}
		if (((PropertyBasedJobInclusionStrategyDescriptor) getDescriptor()).cloudbeesFolders) {
			String jobViewName = FolderPropertyLoader.getJobGroupName(decisionLogger, job);
			if (jobViewName == null) {
				return false;
			}
			boolean match = name.equals(jobViewName);
			if (match) {
				decisionLogger.addDecisionLog(4, "Job is included in JobGroup ...");
			} else {
				decisionLogger.addDecisionLog(4, "Job is not included in JobGroup ...");
			}
			return match;
		} else {
			return false;
		}
	}

	public static ListBoxModel getPropertyBasesJobGroups() {
		List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
		ListBoxModel strategies = new ListBoxModel();
		for (JobGroup jobGroup : jobGroups) {
			JobInclusionStrategy inclusionStrategy = jobGroup.getJobGroupStrategy();
			if (inclusionStrategy instanceof PropertyBasedJobInclusionStrategy) {
				strategies.add(((PropertyBasedJobInclusionStrategy) inclusionStrategy).getName());
			}
		}
		return strategies;
	}

}
