package jenkins.advancedqueue;

import jenkins.advancedqueue.priority.PriorityStrategy;

public interface PriorityConfigurationCallback {

	PriorityConfigurationCallback setPrioritySelection(int priority);

	PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId, PriorityStrategy reason);
	
	PriorityConfigurationCallback addDecisionLog(String log);

}
