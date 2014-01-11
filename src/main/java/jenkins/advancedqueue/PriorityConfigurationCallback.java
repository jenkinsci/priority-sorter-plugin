package jenkins.advancedqueue;

import jenkins.advancedqueue.priority.PriorityStrategy;

public interface PriorityConfigurationCallback extends DecisionLogger {

	PriorityConfigurationCallback setPrioritySelection(int priority);

	PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId, PriorityStrategy reason);
	
}
