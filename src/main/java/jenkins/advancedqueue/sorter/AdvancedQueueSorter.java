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
import hudson.model.Describable;
import hudson.model.Descriptor;
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

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class AdvancedQueueSorter extends QueueSorter implements
		Describable<AdvancedQueueSorter> {

	static class AdvancedQueueSorterDescriptor extends
			Descriptor<AdvancedQueueSorter> {

		// Keeps track of what weighted-prio each buildableItems item has
		Map<Integer, Float> item2weight = new HashMap<Integer, Float>();

		@Override
		public String getDisplayName() {
			return "AdvancedQueueSorterDescriptor";
		}

	}

	private final AdvancedQueueSorterDescriptor descriptor = new AdvancedQueueSorterDescriptor();

	public Descriptor<AdvancedQueueSorter> getDescriptor() {
		return descriptor;
	}

	public AdvancedQueueSorter() {
		super();
		descriptor.load();
	}

	@Override
	public void sortBuildableItems(List<BuildableItem> items) {
		// Handle Legacy mode
		if (PrioritySorterConfiguration.get().getLegacyMode()) {
			new PrioritySorterQueueSorter().sortBuildableItems(items);
		}
		// Sort
		Collections.sort(items, new Comparator<BuildableItem>() {
			public int compare(BuildableItem o1, BuildableItem o2) {
				float o1weight = descriptor.item2weight.get(o1.id);
				float o2weight = descriptor.item2weight.get(o2.id);
				if (o1weight > o2weight) {
					return 1;
				}
				if (o1weight < o2weight) {
					return -1;
				}
				return (int) (o1.getInQueueSince() - o2.getInQueueSince());
			}
		});
	}

	public void onEnterWaiting(WaitingItem wi) {
		final SorterStrategy prioritySorterStrategy = SorterStrategy
				.getPrioritySorterStrategy(PrioritySorterConfiguration.get()
						.getStrategy());
		final float weight = prioritySorterStrategy.onNewItem(wi);
		descriptor.item2weight.put(wi.id, weight);
		descriptor.save();
	}

	public void onLeft(LeftItem li) {
		final SorterStrategy prioritySorterStrategy = SorterStrategy
				.getPrioritySorterStrategy(PrioritySorterConfiguration.get()
						.getStrategy());
		Float weight = descriptor.item2weight.remove(li.id);
		descriptor.save();
		if (li.isCancelled()) {
			prioritySorterStrategy.onCanceledItem(li);
		} else {
			prioritySorterStrategy.onStartedItem(li, weight);
		}

	}

	static public AdvancedQueueSorter get() {
		return QueueSorter.all().get(AdvancedQueueSorter.class);
	}

}
