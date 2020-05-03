/*
 * The MIT License
 *
 * Copyright (c) 2013, Magnus Sandberg
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
package jenkins.advancedqueue.priority.strategy;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Descriptor.FormException;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.JobGroup;
import jenkins.advancedqueue.Messages;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PriorityConfigurationCallback;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.priority.PriorityStrategy;
import net.sf.json.JSONObject;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
public class PriorityJobProperty extends JobProperty<Job<?, ?>> {

	public final boolean useJobPriority;
	public final int priority;

	@Override
	public JobProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws FormException {
		return super.reconfigure(req, form);
	}

	@DataBoundConstructor
	public PriorityJobProperty(boolean useJobPriority, int priority) {
		this.useJobPriority = useJobPriority;
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public boolean getUseJobPriority() {
		return useJobPriority;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {
		
		private PriorityConfigurationCallback dummyCallback = new PriorityConfigurationCallback() {
			
			public PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId, PriorityStrategy reason) {
				return this;
			}
			
			public PriorityConfigurationCallback setPrioritySelection(int priority) {
				return this;
			}
			
			public PriorityConfigurationCallback addDecisionLog(int indent, String log) {
				return this;
			}

			public PriorityConfigurationCallback setPrioritySelection(int priority, long sortAsInQueueSince,
					int jobGroupId, PriorityStrategy reason) {
				return this;
			}
		};
		
		@Override
		public String getDisplayName() {
			return Messages.AdvancedQueueSorterJobProperty_displayName();
		}

		public int getDefault() {
			return PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
		}

		public ListBoxModel getPriorities() {
			ListBoxModel items = PrioritySorterConfiguration.get().doGetPriorityItems();
			return items;
		}

		public boolean isUsed(Job<?,?> owner) {
			PriorityConfiguration configuration = PriorityConfiguration.get();
			JobGroup jobGroup = configuration.getJobGroup(dummyCallback, owner);
			if(jobGroup != null && jobGroup.isUsePriorityStrategies()) {
				List<PriorityStrategy> priorityStrategies = jobGroup.getPriorityStrategies();
				for (PriorityStrategy priorityStrategy : priorityStrategies) {
					if(priorityStrategy instanceof JobPropertyStrategy) {
						return true;
					}
				}
			}
			return false;
		}

		@Initializer(before = InitMilestone.PLUGINS_STARTED)
		public static void addAliases() {
			// Moved in 3.0 when JobPropertyStrategy was added
			Items.XSTREAM2.addCompatibilityAlias("jenkins.advancedqueue.AdvancedQueueSorterJobProperty", PriorityJobProperty.class);
		}
	}
}
