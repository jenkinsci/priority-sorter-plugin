package jenkins.advancedqueue.strategy;

import hudson.Extension;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.SorterStrategy;

@Extension
public class WFQStrategy extends FQBaseStrategy {

	private final SorterStrategy strategy = new SorterStrategy("WFQ", Messages.SorterStrategy_WFQ_displayName());
	
	public SorterStrategy getSorterStrategy() {
		return strategy;
	}
	
	float getStepSize(int priority) {
		// If WFQ a lower priority is more important than a higher priority 
		// so we must step higher priorities faster than lower ones
		//
		// The step-size for the priority is dependent on its priority
		float stepSize = MIN_STEP_SIZE * (float) priority;		
		return stepSize;
	}

}
