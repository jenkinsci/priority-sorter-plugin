package jenkins.advancedqueue;

import java.util.List;

import hudson.Extension;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.QueueSorter;
import hudson.queueSorter.PrioritySorterQueueSorter;

@Extension
public class PriorityQueueSorter extends QueueSorter {

	@Override
	public void sortBuildableItems(List<BuildableItem> queue) {
		if(PrioritySorterConfiguration.get().getLegacyMode()) {
			new PrioritySorterQueueSorter().sortBuildableItems(queue);
		} else {
			new AdvancedQueueSorter().sortBuildableItems(queue);
		}
		
	}

}
