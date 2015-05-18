package jenkins.advancedqueue.jobrestrictions;

import hudson.Extension;
import hudson.model.Queue.BuildableItem;
import hudson.model.Run;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.advancedqueue.util.PrioritySorterUtil;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestriction;
import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.restrictions.JobRestrictionDescriptor;

/**
 * @since 3.3
 */
public class PrioritySorterRestriction extends JobRestriction {

	@Extension(optional = true)
	public static class DescriptorImpl extends JobRestrictionDescriptor {

		@Override
		public String getDisplayName() {
			return "Priority from PrioritySorter";
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
				value = Integer.valueOf(strValue);
			} catch (NumberFormatException e) {
				// Use default value
			}
			ListBoxModel items = PrioritySorterUtil.fillPriorityItems(value, PrioritySorterConfiguration.get()
					.getStrategy().getNumberOfPriorities());
			return items;
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
		ItemInfo item = QueueItemCache.get().getItem(buildableItem.id);
		int priority = item.getPriority();
		return priority >= fromPriority && priority <= toPriority;
	}

	@Override
	public boolean canTake(Run run) {
		return true;
	}
}