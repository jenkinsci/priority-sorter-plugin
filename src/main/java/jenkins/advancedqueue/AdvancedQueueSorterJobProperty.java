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
package jenkins.advancedqueue;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
public class AdvancedQueueSorterJobProperty extends JobProperty<AbstractProject<?, ?>> {

	private final static Logger LOGGER = Logger.getLogger(AdvancedQueueSorterJobProperty.class.getName());

	public final boolean useJobPriority;
	public final int priority;

	@Override
	public JobProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws FormException {
		return super.reconfigure(req, form);
	}

	@DataBoundConstructor
	public AdvancedQueueSorterJobProperty(boolean useJobPriority, int priority) {
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

		public boolean isUsed() {
			PrioritySorterConfiguration configuration = PrioritySorterConfiguration.get();
			return configuration.getAllowPriorityOnJobs();
		}
	}
}
