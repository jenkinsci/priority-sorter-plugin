package jenkins.advancedqueue;

public interface PriorityConfigurationCallback {

	PriorityConfigurationCallback setPrioritySelection(int priority);

	PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId);

}
