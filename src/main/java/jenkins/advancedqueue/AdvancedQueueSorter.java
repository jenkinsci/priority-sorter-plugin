package jenkins.advancedqueue;

import hudson.model.Job;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.QueueSorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AdvancedQueueSorter extends QueueSorter {

	// Keeps track of what weighted-prio each buildableItems item has
	static Map<Integer, Float> item2weight = new HashMap<Integer, Float>();
	// Keeps track of how many buildableItems slots of each base-priority we have counted
	static Map<Integer, Integer> prio2num = new HashMap<Integer, Integer>();

	@Override
	public void sortBuildableItems(List<BuildableItem> items) {
		////
		// Handle FIFO
		SorterStrategy strategy = PrioritySorterConfiguration.get().getStrategy();
		if(SorterStrategy.FIFO == strategy) {
			Collections.sort(items, new Comparator<BuildableItem>() {
				public int compare(BuildableItem o1, BuildableItem o2) {
					return (int) (o1.getInQueueSince() - o2.getInQueueSince());
				}
			});
			return;
		}
		////
		// Handle ABSOLUTE
		if(SorterStrategy.ABSOLUTE == strategy) {
			final PriorityConfiguration configuration = PriorityConfiguration.get();
			Collections.sort(items, new Comparator<BuildableItem>() {
				public int compare(BuildableItem o1, BuildableItem o2) {
					int p1 = configuration.getPriority((Job<?, ?>) o1.task);
					int p2 = configuration.getPriority((Job<?, ?>) o2.task);
					if(p1 > p2) {
						return 1;
					}
					if(p1 < p2) {
						return -1;
					}
					return (int) (o1.getInQueueSince() - o2.getInQueueSince());
				}
			});
			return;
		}		
		// If the queue is empty reset the internal priority counters
		if(items.isEmpty()) {
			item2weight.clear();
			prio2num.clear();
			return;
		}
		////
		// Handle *FQ
		//
		// How many of priority slots per cycle do we have?
		int numberOfPriorities = PrioritySorterConfiguration.get().getNumberOfPriorities();
		// Get the minimum prio we can use due to already started items - this forms the prio-baseline
		float minPrioToAssign = getLastStartedPrio(items, item2weight);

		// Calculate the minimum prio we can use for each priority-group
		for (int priority = 1; priority <= numberOfPriorities; priority++) {
			// The step-size for the priority
			float stepSize = priority;
			// Calculate the first usable prio for this prio-group
			// ... and yes - this can be done better
			Integer current = prio2num.get(priority);
			if(current == null) {
				current = 1;
			}
			float prioToUse = priority*current;		
			while(prioToUse < minPrioToAssign) {
				current++;
				prioToUse += stepSize;
			}
			prio2num.put(priority, current);
		}
		// Now let's assign prio to items
		for (BuildableItem buildableItem : items) {
			// If we haven't already seen this BuildableItem calculate its priority
			if(!item2weight.containsKey(buildableItem.id)) {
				//
				int priority = PriorityConfiguration.get().getPriority((Job<?, ?>) buildableItem.task);
				// Calculate the prio for this item based on how many of this prio 
				// has been added already and then increase to count for the next item
				Integer current = prio2num.get(priority);
				float weighted_priority;
				if(SorterStrategy.WFQ == strategy) {
					weighted_priority = current * priority + (priority / (float)numberOfPriorities);
				} else { // FQ
					weighted_priority = current + (priority / (float)numberOfPriorities);
				}
				item2weight.put(buildableItem.id, weighted_priority);
				current++;
				prio2num.put(priority, current);
			}
		}
		// Now sort on weight
		Collections.sort(items, new Comparator<BuildableItem>() {
			public int compare(BuildableItem o1, BuildableItem o2) {
				if(item2weight.get(o1.id) > item2weight.get(o2.id)) {
					return 1;
				}
				if(item2weight.get(o1.id) < item2weight.get(o2.id)) {
					return -1;
				}
				return (int) (o1.getInQueueSince() - o2.getInQueueSince());
			}
		});
	}
	
	private float getLastStartedPrio(List<BuildableItem> items, Map<Integer, Float> item2prio) {
		float maxRemovedPrio = 0F;
		if(items.isEmpty()) {
			if(!item2prio.isEmpty()) {
				return Collections.max(item2prio.values());
			}
		} else {
			// Get the lowest prio of the items in the queue that we have seen before
			float currentlyMinPrio = Float.MAX_VALUE;
			for (BuildableItem buildableItem : items) {
				int id = buildableItem.id;
				if(item2prio.containsKey(id) && item2prio.get(id) < currentlyMinPrio) {
					currentlyMinPrio = item2prio.get(id);
				}
			}
			// Now find the among the seen items the highest prio missing, ie started or possibly deleted 
			// Value also need be be less than "currentlyMinPrio" not to count items deleted from the middle of the queue
			Set<Entry<Integer,Float>> entrySet = item2prio.entrySet();
			for (Entry<Integer, Float> entry : entrySet) {
				Integer id = entry.getKey();
				float prio = entry.getValue();
				boolean itemFound = false;
				for (BuildableItem buildableItem : items) {
					if(id.equals(buildableItem.id)) {
						itemFound = true;
					}
				}
				if(!itemFound && prio < currentlyMinPrio && prio > maxRemovedPrio) {
					maxRemovedPrio = prio;
				}
			}
		}
		return maxRemovedPrio;
	}
}
