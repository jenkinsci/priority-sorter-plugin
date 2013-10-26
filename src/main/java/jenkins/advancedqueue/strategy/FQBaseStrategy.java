package jenkins.advancedqueue.strategy;

import hudson.model.Job;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;

import java.util.HashMap;
import java.util.Map;

import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterStrategy;

abstract public class FQBaseStrategy extends PrioritySorterStrategy {
	
	// 
	static final protected float MIN_STEP_SIZE = 0.00001F;
	
	// Keeps track on the last assigned weight for a given priority
	static Map<Integer, Float> prio2weight = new HashMap<Integer, Float>();

	// Keeps track on the max weight of started jobs
	private float maxStartedWeight = 1F;
	
	public void onStartedItem(LeftItem item, float weight) {
		maxStartedWeight = Math.max(maxStartedWeight, weight);
	}

	public float onNewItem(WaitingItem item) {
		int priority = PriorityConfiguration.get().getPriority((Job<?, ?>) item.task);
		float minimumWeightToAssign = getMinimumWeightToAssign(priority);
		float weightToUse = getWeightToUse(priority, minimumWeightToAssign);
		prio2weight.put(priority, weightToUse);
		return weightToUse;
	}

	protected float getMinimumWeightToAssign(int priority) {
		Float minWeight = prio2weight.get(priority);
		if(minWeight == null) {
			return maxStartedWeight;
		}
		return Math.max(maxStartedWeight, minWeight);
	}
	
	protected float getWeightToUse(int priority, float minimumWeightToAssign) {
		float stepSize = getStepSize(priority);
		double weight = Math.ceil(minimumWeightToAssign / stepSize) * stepSize + stepSize;
		// Protect us from values going through the roof if we run for a very long time
		// This below might leave some jobs in the queue with very large weight
		// this probably improbable to happen so let's do it like this for now ...
		if(Double.POSITIVE_INFINITY == weight) {
			maxStartedWeight = 1F;
			prio2weight.clear();
			return getWeightToUse(priority, minimumWeightToAssign);
		}
		return (float) weight;
	}

	abstract float getStepSize(int priority);
	
}
