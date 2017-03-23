package jenkins.advancedqueue;

import hudson.Plugin;
import hudson.model.Job;
import hudson.model.Queue;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;

class PriorityConfigurationPlaceholderTaskHelper {

	boolean isPlaceholderTask(Queue.Task task) {
		return isPlaceholderTaskUsed() && task instanceof ExecutorStepExecution.PlaceholderTask;
	}

	PriorityConfigurationCallback getPriority(ExecutorStepExecution.PlaceholderTask task,
			PriorityConfigurationCallback priorityCallback) {
		Job<?, ?> job = (Job<?, ?>) task.getOwnerTask();
		ItemInfo itemInfo = QueueItemCache.get().getItem(job.getName());

		// Can be null if job didn't run yet

		if (itemInfo != null) {
			priorityCallback.setPrioritySelection(itemInfo.getPriority());
		} else {
			priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority());
		}

		return priorityCallback;
	}

	static boolean isPlaceholderTaskUsed() {
		Plugin plugin = Jenkins.getInstance().getPlugin("workflow-durable-task-step");
		return plugin != null && plugin.getWrapper().isActive();
	}

}
