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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Queue.BuildableItem;
import hudson.model.Run;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.Messages;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.advancedqueue.util.PrioritySorterUtil;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;

/**
 * Extends the {@link JobRestriction} from  <a href="https://plugins.jenkins.io/job-restrictions/">Job Restrictions Plugin</a>
 * making it possible to restrict Node usage based on priority. 
 * 
 * @author Magnus Sandberg
 * @since 3.3
 */
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID",
                    justification="Common usage in Jenkins to not include SE_NO_SERIALVERSIONID")
public class PrioritySorterRestriction extends JobRestriction {
	
	private final static Logger LOGGER = Logger.getLogger(PrioritySorterRestriction.class.getName());

	@Extension(optional = true)
	public static class DescriptorImpl extends JobRestrictionDescriptor {

		@Override
		public String getDisplayName() {
			return Messages.Priority_from_prioritySorter();
		}

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
			return PrioritySorterUtil.fillPriorityItems(value, PrioritySorterConfiguration.get()
					.getStrategy().getNumberOfPriorities());
		}

	}

	private int fromPriority;
	
	private int toPriority;

	public int getFromPriority() {
		return fromPriority;
	}

	public int getToPriority() {
		return toPriority;
	}

	@DataBoundConstructor
	public PrioritySorterRestriction(int fromPriority, int toPriority) {
		this.fromPriority = fromPriority;
		this.toPriority = toPriority;
	}

	@Override
	public boolean canTake(BuildableItem buildableItem) {
		ItemInfo item = QueueItemCache.get().getItem(buildableItem.getId());
		if(item == null) {
			LOGGER.warning("Missing ItemInfo for [" + buildableItem.task.getDisplayName() + "] allowing execution.");
			return true;
		}
		int priority = item.getPriority();
		return priority >= fromPriority && priority <= toPriority;
	}

	@Override
	public boolean canTake(Run run) {
		return true;
	}
}
