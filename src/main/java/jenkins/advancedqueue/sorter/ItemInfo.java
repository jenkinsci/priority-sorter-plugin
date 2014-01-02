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

import static jenkins.advancedqueue.ItemTransitionLogger.logBlockedItem;
import static jenkins.advancedqueue.ItemTransitionLogger.logBuilableItem;

import java.util.ArrayList;
import java.util.List;

import hudson.model.Queue.Item;
import jenkins.advancedqueue.PriorityConfigurationCallback;
import jenkins.advancedqueue.priority.PriorityStrategy;

/**
 * Used to store info about a Queue.Item and related information calculated by the Plugin
 * 
 * @author Magnus Sandberg
 * @since 2.3
 */
public class ItemInfo implements PriorityConfigurationCallback, SorterStrategyCallback, Comparable<ItemInfo> {

	private int itemId;

	private long inQueueSince;

	private int jobGroupId;

	private PriorityStrategy priorityStrategy;

	private String jobName;

	private float weight;

	private int priority;

	private ItemStatus itemStatus;
	
	private List<String> decisionLog = new ArrayList<String>(10);

	ItemInfo(Item item) {
		this.itemId = item.id;
		this.inQueueSince = item.getInQueueSince();
		this.jobName = item.task.getName();
		this.itemStatus = ItemStatus.WAITING;
	}

	public PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId, PriorityStrategy reason) {
		this.priority = priority;
		this.jobGroupId = jobGroupId;
		this.priorityStrategy = reason;
		return this;
	}

	public PriorityConfigurationCallback addDecisionLog(String log) {
		this.decisionLog.add(log);
		return this;
	}

	public PriorityConfigurationCallback setPrioritySelection(int priority) {
		setPrioritySelection(priority, -1, null);
		return this;
	}

	public SorterStrategyCallback setWeightSelection(float weight) {
		this.weight = weight;
		return this;
	}

	public void setBuildable() {
		itemStatus = ItemStatus.BUILDABLE;
		logBuilableItem(this);
	}

	public void setBlocked() {
		itemStatus = ItemStatus.BLOCKED;
		logBlockedItem(this);
	}

	public int getItemId() {
		return itemId;
	}

	public long getInQueueSince() {
		return inQueueSince;
	}

	public int getJobGroupId() {
		return jobGroupId;
	}

	public PriorityStrategy getPriorityStrategy() {
		return priorityStrategy;
	}

	public String getJobName() {
		return jobName;
	}

	public float getWeight() {
		return weight;
	}

	public int getPriority() {
		return priority;
	}

	public ItemStatus getItemStatus() {
		return itemStatus;
	}

	public int compareTo(ItemInfo o) {
		if(this.getWeight() == o.getWeight()) {
			if(this.getInQueueSince() == o.getInQueueSince()) {
				return Integer.compare(this.getItemId(), o.getItemId());
			}
			return Long.compare(this.getInQueueSince(), o.getInQueueSince());
		}
		return Float.compare(this.getWeight(), o.getWeight());
	}

	@Override
	public String toString() {
		return String.format("Id: %s JobName: %s, jobGroupId: %s, priority: %s, weight: %s, status: %s", itemId,
				jobName, jobGroupId, priority, weight, itemStatus);
	}
	
	public String getDescisionLog() {
		StringBuffer buffer = new StringBuffer();
		for (String  log : decisionLog) {
			buffer.append(log).append("\n");
		}
		return buffer.toString();
	}

}
