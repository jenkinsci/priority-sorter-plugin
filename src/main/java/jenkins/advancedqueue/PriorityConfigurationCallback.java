package jenkins.advancedqueue;

import jenkins.advancedqueue.priority.PriorityStrategy;

public interface PriorityConfigurationCallback extends DecisionLogger {

    PriorityConfigurationCallback setPrioritySelection(int priority);

    PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId, PriorityStrategy reason);

    PriorityConfigurationCallback setPrioritySelection(int priority, long sortAsInQueueSince, int jobGroupId, PriorityStrategy reason);

}
