/*
 * The MIT License
 *
 * Copyright (c) 2013, Magnus Sandberg
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
package jenkins.advancedqueue.sorter;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Item;
import hudson.model.Queue.LeftItem;
import hudson.model.queue.QueueSorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import static jenkins.advancedqueue.ItemTransitionLogger.*;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class AdvancedQueueSorter extends QueueSorter {

	private final static Logger LOGGER = Logger.getLogger("PrioritySorter.Queue.Sorter");

	public AdvancedQueueSorter() {
	}

	static public void init() {
		List<BuildableItem> items = Queue.getInstance().getBuildableItems();
		// Sort the queue in the order the items entered the queue
		// so that onNewItem() happens in the correct order below
		Collections.sort(items, new Comparator<BuildableItem>() {
			public int compare(BuildableItem o1, BuildableItem o2) {
				return (int) (o1.getInQueueSince() - o2.getInQueueSince());
			}
		});
		AdvancedQueueSorter advancedQueueSorter = AdvancedQueueSorter.get();
		for (BuildableItem item : items) {
			advancedQueueSorter.onNewItem(item);
			// Listener called before we get here so make sure we mark buildable
			QueueItemCache.get().getItem(item.id).setBuildable();
		}
		LOGGER.info("Initialized the QueueSorter with " + items.size() + " Buildable Items");
	}

	@Override
	public void sortBuildableItems(List<BuildableItem> items) {

		Collections.sort(items, new Comparator<BuildableItem>() {
			public int compare(BuildableItem o1, BuildableItem o2) {
				ItemInfo item1 = QueueItemCache.get().getItem(o1.id);
				ItemInfo item2 = QueueItemCache.get().getItem(o2.id);
				if(item1 == null || item2 == null) {
					LOGGER.warning("Requested to sort unknown items, sorting on queue-time only.");
					return new Long(o1.getInQueueSince()).compareTo(o2.getInQueueSince());
				}
				return item1.compareTo(item2);
			}
		});
		//
		if (items.size() > 0 && LOGGER.isLoggable(Level.FINE)) {
			float minWeight = QueueItemCache.get().getItem(items.get(0).id).getWeight();
			float maxWeight = QueueItemCache.get().getItem(items.get(items.size() - 1).id).getWeight();
			LOGGER.log(Level.FINE, "Sorted {0} Buildable Items with Min Weight {1} and Max Weight {2}", new Object[] {
					items.size(), minWeight, maxWeight });
		}
		//
		if (items.size() > 0 && LOGGER.isLoggable(Level.FINER)) {
			StringBuffer queueStr = new StringBuffer("Queue:\n"
					+ "+----------------------------------------------------------------------+\n"
					+ "|   Item Id  | Priority |        Weight        |         Job Name       \n"
					+ "+----------------------------------------------------------------------+\n");
			for (BuildableItem item : items) {
				ItemInfo itemInfo = QueueItemCache.get().getItem(item.id);
				queueStr.append(String.format("| %10d | %8d | %20.5f | %s \n",
						item.id, itemInfo.getPriority(), itemInfo.getWeight(), itemInfo.getJobName()));

			}
			queueStr.append("+----------------------------------------------------------------------+");
			LOGGER.log(Level.FINER, queueStr.toString());
		}
	}

	/**
	 * Returned the calculated, cached, weight or calculates the weight if missing. Should only be
	 * called when the value should already be there, if the item is new {@link #onNewItem(Item)} is
	 * the method to call.
	 *
	 * @param item the item to get the weight for
	 * @return the calculated weight
	 */
	private float getCalculatedWeight(BuildableItem item) {
		try {
			return QueueItemCache.get().getItem(item.id).getWeight();
		} catch (NullPointerException e) {
			onNewItem(item);
			return QueueItemCache.get().getItem(item.id).getWeight();
		}
	}

	public void onNewItem(@Nonnull Item item) {
		final SorterStrategy prioritySorterStrategy = PrioritySorterConfiguration.get().getStrategy();
		ItemInfo itemInfo = new ItemInfo(item);
		PriorityConfiguration.get().getPriority(item, itemInfo);
		prioritySorterStrategy.onNewItem(item, itemInfo);
		QueueItemCache.get().addItem(itemInfo);
		logNewItem(itemInfo);
	}

	public void onLeft(@Nonnull LeftItem li) {
		ItemInfo itemInfo = QueueItemCache.get().removeItem(li.id);
                if (itemInfo == null) {
                    LOGGER.log(Level.WARNING, "Received the onLeft() notification for the item from outside the QueueItemCache: {0}. " +
                            "Cannot process this item, Priority Sorter Strategy will not be invoked", li);
                    return;
                }
                
                final SorterStrategy prioritySorterStrategy = PrioritySorterConfiguration.get().getStrategy();
		if (li.isCancelled()) {
			prioritySorterStrategy.onCanceledItem(li);
			logCanceledItem(itemInfo);
		} else {
			Float weight = itemInfo.getWeight();
			StartedJobItemCache.get().addItem(itemInfo, li.outcome.getPrimaryWorkUnit());
			prioritySorterStrategy.onStartedItem(li, weight);
			logStartedItem(itemInfo);
		}
	}

	static public AdvancedQueueSorter get() {
		return QueueSorter.all().get(AdvancedQueueSorter.class);
	}

}
