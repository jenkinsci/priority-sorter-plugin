/*
 * The MIT License
 *
 * Copyright (c) 2013, Magnus Sandberg and contributors
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
package jenkins.advancedqueue.sorter.strategy;

import hudson.model.Queue;
import hudson.model.Queue.LeftItem;

import java.util.HashMap;
import java.util.Map;

import jenkins.advancedqueue.sorter.SorterStrategyCallback;

/**
 * Scheduler based on Fair Queuing algorithm.
 * @author Magnus Sandberg
 * @since 2.0
 */
abstract public class FQBaseStrategy extends MultiBucketStrategy {
	// The equivalent of a packet size for a network scheduler.
	static final protected float MIN_STEP_SIZE = 0.00001F;
	// Keeps track on the last assigned weight for a given priority
	static final protected Map<Integer, Float> prio2weight = new HashMap<Integer, Float>();
	static final private float MIN_STARTED_WEIGHT = 1F;
	// Keeps track on the max weight of started jobs
	static protected float maxStartedWeight = MIN_STARTED_WEIGHT;

	public FQBaseStrategy() {
	}

	public FQBaseStrategy(int numberOfPriorities, int defaultPriority) {
		super(numberOfPriorities, defaultPriority);
	}

	@Override
	public void onStartedItem(LeftItem item, float weight) {
		maxStartedWeight = Math.max(maxStartedWeight, weight);
	}

	public SorterStrategyCallback onNewItem(Queue.Item item, SorterStrategyCallback weightCallback) {
		int priority = weightCallback.getPriority();
		float minimumWeightToAssign = getMinimumWeightToAssign(priority);
		float weightToUse = getWeightToUse(priority, minimumWeightToAssign);
		prio2weight.put(priority, weightToUse);
		return weightCallback.setWeightSelection(weightToUse);
	}

	protected float getMinimumWeightToAssign(int priority) {
		Float minWeight = prio2weight.get(priority);
		if (minWeight == null) {
			return maxStartedWeight;
		}
		return Math.max(maxStartedWeight, minWeight);
	}

	protected float getWeightToUse(int priority, float minimumWeightToAssign) {
		float weight = minimumWeightToAssign * (1F + getStepSize(priority));
		// Protect us from values going through the roof if we run for a very
		// long time
		// This below might leave some jobs in the queue with very large weight
		// this probably improbable to happen so let's do it like this for now
		// ...
		if (Float.POSITIVE_INFINITY == weight) {
			maxStartedWeight = MIN_STARTED_WEIGHT;
			prio2weight.clear();
			return MIN_STARTED_WEIGHT;
		}
		return weight;
	}

	abstract float getStepSize(int priority);
}
