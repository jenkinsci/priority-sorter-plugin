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

import hudson.model.Queue.Item;
import jenkins.advancedqueue.PriorityConfigurationCallback;

/**
 *  Used to store info about a Queue.Item and related information calculated by the Plugin
 *  
 * @author Magnus Sandberg
 * @since 2.3
 */
public class ItemInfo implements PriorityConfigurationCallback, SorterStrategyCallback {

	private int itemId;

	private int jobGroupId;

	private String jobName;

	private float weight;

	private int priority;
	
	private ItemStatus itemStatus;

	ItemInfo(Item item) {
		this.itemId = item.id;
		this.jobName = item.task.getName();
		this.itemStatus = ItemStatus.WAITING;
	}

	public PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId) {
		this.priority = priority;
		this.jobGroupId = jobGroupId;
		return this;
	}

	public PriorityConfigurationCallback setPrioritySelection(int priority) {
		setPrioritySelection(priority, -1);
		return this;
	}

	public SorterStrategyCallback setWeightSelection(float weight) {
		this.weight = weight;
		return this;
	}

	public int getItemId() {
		return itemId;
	}

	public int getJobGroupId() {
		return jobGroupId;
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

	public void setItemStatus(ItemStatus itemStatus) {
		this.itemStatus = itemStatus;
	}
	
	

}
