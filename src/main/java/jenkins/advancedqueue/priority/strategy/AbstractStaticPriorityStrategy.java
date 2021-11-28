package jenkins.advancedqueue.priority.strategy;

import hudson.model.Descriptor;
import hudson.model.Queue;
import hudson.util.ListBoxModel;
import jenkins.advancedqueue.PriorityCalculationsUtil;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.model.Jenkins;

abstract public class AbstractStaticPriorityStrategy extends PriorityStrategy {

	static public class AbstractStaticPriorityStrategyDescriptor extends Descriptor<PriorityStrategy> {

		private final String displayName;

		protected AbstractStaticPriorityStrategyDescriptor(String displayName) {
			this.displayName = displayName;
		}

		public ListBoxModel getPriorities() {
			ListBoxModel items = PrioritySorterConfiguration.get().doGetPriorityItems();
			return items;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

	};

	@SuppressWarnings("unchecked")
	public Descriptor<PriorityStrategy> getDescriptor() {
		return Jenkins.get().getDescriptor(this.getClass());
	}

	private int priority;

	@Override
	public void numberPrioritiesUpdates(int oldNumberOfPriorities, int newNumberOfPriorities) {
		priority = PriorityCalculationsUtil.scale(oldNumberOfPriorities, newNumberOfPriorities, priority);

	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int getPriority(Queue.Item item) {
		return priority;
	}

}
