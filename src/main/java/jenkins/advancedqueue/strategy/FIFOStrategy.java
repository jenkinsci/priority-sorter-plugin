package jenkins.advancedqueue.strategy;

import hudson.Extension;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.WaitingItem;
import jenkins.advancedqueue.PrioritySorterStrategy;
import jenkins.advancedqueue.SorterStrategy;

@Extension
public class FIFOStrategy extends PrioritySorterStrategy {

	private final SorterStrategy strategy = new SorterStrategy("FIFO", Messages.SorterStrategy_FIFO_displayName());
	
	public SorterStrategy getSorterStrategy() {
		return strategy;
	}

	public float onNewItem(WaitingItem item) {
		return item.getInQueueSince();
	}

}
