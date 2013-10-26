package jenkins.advancedqueue.strategy;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Queue.WaitingItem;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterStrategy;
import jenkins.advancedqueue.SorterStrategy;

@Extension
public class AbsoluteStrategy extends PrioritySorterStrategy {

	private final SorterStrategy strategy = new SorterStrategy("ABSOLUTE", Messages.SorterStrategy_ABSOLUTE_displayName());
	
	public SorterStrategy getSorterStrategy() {
		return strategy;
	}
	
	public float onNewItem(WaitingItem item) {
		return PriorityConfiguration.get().getPriority((Job<?, ?>) item.task);
	}

}
