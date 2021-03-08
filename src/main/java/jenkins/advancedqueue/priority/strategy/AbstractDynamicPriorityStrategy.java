package jenkins.advancedqueue.priority.strategy;

import hudson.model.Descriptor;
import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.model.Jenkins;

abstract public class AbstractDynamicPriorityStrategy extends PriorityStrategy {

	static public class AbstractDynamicPriorityStrategyDescriptor extends Descriptor<PriorityStrategy> {

		private final String displayName;

		protected AbstractDynamicPriorityStrategyDescriptor(String displayName) {
			this.displayName = displayName;
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

	@Override
	public void numberPrioritiesUpdates(int oldNumberOfPriorities, int newNumberOfPriorities) {
		// ignore as we do not store/control the priority
	}

}
