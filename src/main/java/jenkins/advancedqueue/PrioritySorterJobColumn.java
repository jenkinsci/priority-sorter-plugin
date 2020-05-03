/*
 * The MIT License
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
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Priority column on the jobs overview page. The column displays priority set
 * for the job and is an easy way to compare the priorities of many jobs.
 */
public class PrioritySorterJobColumn extends ListViewColumn {
	
	@DataBoundConstructor
	public PrioritySorterJobColumn() {
	}

	public String getPriority(final Job<?, ?> job) {
		ItemInfo itemInfo = QueueItemCache.get().getItem(job.getName());
		if(itemInfo == null) {
			return "Pending"; // You need to run a Job
		}
		return Integer.toString(itemInfo.getPriority());
	}

	@Extension
	public static class DescriptorImpl extends ListViewColumnDescriptor {

		@Override
		public String getDisplayName() {
			return "Priority Value";
		}

		@Override
		public boolean shownByDefault() {
			return false;
		}

		@Initializer(before = InitMilestone.PLUGINS_STARTED)
		public static void addAliases() {
			// moved in 3.0 everything in hudson.* is deprecated
			Items.XSTREAM2.addCompatibilityAlias("hudson.queueSorter.PrioritySorterJobColumn", PrioritySorterJobColumn.class);		
		}
	}
}
