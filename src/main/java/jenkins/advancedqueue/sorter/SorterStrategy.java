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

import hudson.ExtensionList;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;

import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;

import org.apache.tools.ant.ExtensionPoint;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
public abstract class SorterStrategy extends ExtensionPoint {

	public abstract SorterStrategyType getSorterStrategy();
	
	/**
	 * Called when a new {@link hudson.model.Item} enters the queue.
	 * 
	 * @param item the {@link hudson.model.WaitingItem} that enters the queue
	 * @return the weight of the item in the queue, lower value will give sooner start
	 */
	public abstract float onNewItem(WaitingItem item);
	
	/**
	 * Called when a {@link hudson.model.Item} leaves the queue and it is started.
	 * 
	 * @param item the {@link hudson.model.LeftItem}
	 * @param weight the weight assigned when the item entered the queue
	 */
	public void onStartedItem(LeftItem item, float weight) {}

	/**
	 * Called when a {@link hudson.model.Item} leaves the queue and it is canceled.
	 */
	public void onCanceledItem(LeftItem item) {};

	public static List<SorterStrategyType> getAllSorterStrategies() {
		ExtensionList<SorterStrategy> all = all();
		ArrayList<SorterStrategyType> strategies = new ArrayList<SorterStrategyType>(all.size());
		for (SorterStrategy prioritySorterStrategy : all) {
			strategies.add(prioritySorterStrategy.getSorterStrategy());
		}
		return strategies;
	}
	
	public static SorterStrategyType getSorterStrategy(String key) {
		List<SorterStrategyType> allSorterStrategies = getAllSorterStrategies();
		for (SorterStrategyType sorterStrategy : allSorterStrategies) {
			if(key.equals(sorterStrategy.getKey())) {
				return sorterStrategy;
			}
		}
		return null;
	}

	public static SorterStrategy getPrioritySorterStrategy(SorterStrategyType sorterStrategy) {
		ExtensionList<SorterStrategy> all = all();
		for (SorterStrategy prioritySorterStrategy : all) {
			if(prioritySorterStrategy.getSorterStrategy().getKey().equals(sorterStrategy.getKey())) {
				return prioritySorterStrategy;
			}
		}
		return null;
	}

	
	/**
     * All registered {@link SorterStrategy}s.
     */
    public static ExtensionList<SorterStrategy> all() {
        return Jenkins.getInstance().getExtensionList(SorterStrategy.class);
    }
}
