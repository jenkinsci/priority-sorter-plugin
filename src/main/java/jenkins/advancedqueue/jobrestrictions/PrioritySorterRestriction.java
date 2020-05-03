/*
 * The MIT License
 *
 * Copyright (c) 2015, Magnus Sandberg
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
package jenkins.advancedqueue.jobrestrictions;

import java.util.logging.Logger;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.Queue.BuildableItem;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.advancedqueue.util.PrioritySorterUtil;

import jenkins.advancedqueue.Messages;

/**
 * Extends the {@link JobRestriction} from  <a href="https://wiki.jenkins-ci.org/display/JENKINS/Job+Restrictions+Plugin">Job Restrictions Plugin</a>
 * making it possible to restrict Node usage based on priority. 
 * 
 * @author Magnus Sandberg
 * @since 3.3
 */
@Extension
public class PrioritySorterRestriction extends JobRestriction {
	
	private static final long serialVersionUID = -9006082445139117284L;

	private final static Logger LOGGER = Logger.getLogger(PrioritySorterRestriction.class.getName());

	private int fromPriority, toPriority;

	public int getFromPriority() {
		return fromPriority;
	}

	public int getToPriority() {
		return toPriority;
	}

	public PrioritySorterRestriction() {
	}

	@DataBoundConstructor
	public PrioritySorterRestriction(int fromPriority, int toPriority) {
		this.fromPriority = fromPriority;
		this.toPriority = toPriority;
	}

	@Override
	public boolean canTake(BuildableItem buildableItem) {
		ItemInfo item = QueueItemCache.get().getItem(buildableItem.getId());
		if (item == null) {
			LOGGER.warning("Missing ItemInfo for [" + buildableItem.task.getDisplayName() + "] allowing execution.");
			return true;
		}
		int priority = item.getPriority();
		return priority >= fromPriority && priority <= toPriority;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean canTake(Run run) {
		return true;
	}

	@Extension(optional = true)
	public static class DescriptorImpl extends JobRestrictionDescriptor {

		@Override
		public String getDisplayName() { return Messages.Priority_from_prioritySorter(); }

		public ListBoxModel doFillFromPriorityItems() {
			return PrioritySorterUtil.fillPriorityItems(PrioritySorterConfiguration.get().getStrategy()
					.getNumberOfPriorities());
		}

		public ListBoxModel doFillToPriorityItems() {
			return PrioritySorterUtil.fillPriorityItems(PrioritySorterConfiguration.get().getStrategy()
					.getNumberOfPriorities());
		}

		public ListBoxModel doUpdateFromPriorityItems(@QueryParameter("value") String strValue) {
			int value = 1;
			try {
				value = Integer.parseInt(strValue);
			} catch (NumberFormatException e) {
				// Use default value
			}
			ListBoxModel items = PrioritySorterUtil.fillPriorityItems(value, PrioritySorterConfiguration.get()
					.getStrategy().getNumberOfPriorities());
			return items;
		}
	}
}
