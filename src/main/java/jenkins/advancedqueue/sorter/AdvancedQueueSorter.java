package jenkins.advancedqueue.sorter;

import hudson.Extension;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueSorter;
import hudson.queueSorter.PrioritySorterQueueSorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.PrioritySorterStrategy;

@Extension
public class AdvancedQueueSorter extends QueueSorter {

	// Keeps track of what weighted-prio each buildableItems item has
	static Map<Integer, Float> item2weight = new HashMap<Integer, Float>();

	@Override
	public void sortBuildableItems(List<BuildableItem> items) {
		// Handle Legacy mode
		if(PrioritySorterConfiguration.get().getLegacyMode()) {
			new PrioritySorterQueueSorter().sortBuildableItems(items);
		} 
		// Sort 
		Collections.sort(items, new Comparator<BuildableItem>() {
			public int compare(BuildableItem o1, BuildableItem o2) {
				float o1weight = item2weight.get(o1.id);
				float o2weight = item2weight.get(o2.id);
				if(o1weight > o2weight) {
					return 1;
				}
				if(o1weight < o2weight) {
					return -1;
				}
				return (int) (o1.getInQueueSince() - o2.getInQueueSince());
			}
		});
	}
	
	public void onEnterWaiting(WaitingItem wi) {
		final PrioritySorterStrategy prioritySorterStrategy = PrioritySorterStrategy
				.getPrioritySorterStrategy(PrioritySorterConfiguration.get()
						.getStrategy());
		final float weight = prioritySorterStrategy.onNewItem(wi);
		item2weight.put(wi.id, weight);
	}

	public void onLeft(LeftItem li) {
		final PrioritySorterStrategy prioritySorterStrategy = PrioritySorterStrategy
				.getPrioritySorterStrategy(PrioritySorterConfiguration.get()
						.getStrategy());
		Float weight = item2weight.remove(li.id);
		if(li.isCancelled()) {
			prioritySorterStrategy.onCanceledItem(li);
		} else {
			prioritySorterStrategy.onStartedItem(li, weight);
		}
		
	}


	static public AdvancedQueueSorter get() {
		return (AdvancedQueueSorter) QueueSorter.all().get(AdvancedQueueSorter.class);
	}

	
}
