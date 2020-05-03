package jenkins.advancedqueue;

import hudson.Plugin;
import hudson.model.Job;
import hudson.model.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;

class PriorityConfigurationPlaceholderTaskHelper {

    private static final Logger LOGGER = Logger.getLogger(PriorityConfigurationPlaceholderTaskHelper.class.getName());
    
	boolean isPlaceholderTask(Queue.Task task) {
		return isPlaceholderTaskUsed() && task instanceof ExecutorStepExecution.PlaceholderTask;
	}

    @Nonnull
    PriorityConfigurationCallback getPriority(@Nonnull ExecutorStepExecution.PlaceholderTask task, @Nonnull PriorityConfigurationCallback priorityCallback) {
        Queue.Task ownerTask = task.getOwnerTask();
        if (ownerTask instanceof Job<?, ?>) {
            Job<?, ?> job = (Job<?, ?>) ownerTask;
            ItemInfo itemInfo = QueueItemCache.get().getItem(job.getName());
            if (itemInfo != null) {
                priorityCallback.setPrioritySelection(itemInfo.getPriority());
            } else {
                priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority());
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Cannot determine priority of the Pipeline Placeholder Task {0}. Its owner task {1} is not a Job (type is {2}). " +
                        "Custom priority will not be set",
                        new Object[] {task, ownerTask, ownerTask});
            }
            priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority());
        }
        return priorityCallback;
    }

	static boolean isPlaceholderTaskUsed() {
		Plugin plugin = Jenkins.get().getPlugin("workflow-durable-task-step");
		return plugin != null && plugin.getWrapper().isActive();
	}

}
