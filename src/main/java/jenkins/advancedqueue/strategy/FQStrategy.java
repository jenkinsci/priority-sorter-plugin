package jenkins.advancedqueue.strategy;

import hudson.Extension;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.SorterStrategy;

@Extension
public class FQStrategy extends FQBaseStrategy {

	private final SorterStrategy strategy = new SorterStrategy("FQ", Messages.SorterStrategy_FQ_displayName());
	
	public SorterStrategy getSorterStrategy() {
		return strategy;
	}
	
	float getStepSize(int priority) {
		// If FQ each priority is equally important 
		// so we basically assign priorities in
		// with round-robin 
		//
		// The step-size for the priority is same for all priorities 
		float stepSize = MIN_STEP_SIZE;
		return stepSize;
	}

}
