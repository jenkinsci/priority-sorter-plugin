package jenkins.advancedqueue;


import hudson.model.Job;
import hudson.model.Queue;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;

class PriorityConfigurationPlaceholderTaskHelper {

    private static boolean placeholderTaskUsed = Jenkins.getInstance().getPlugin("workflow-durable-task-step") != null;

    boolean isPlaceholderTask(Queue.Task task) {
        return placeholderTaskUsed && task instanceof  ExecutorStepExecution.PlaceholderTask;
    }

    PriorityConfigurationCallback getPriority(ExecutorStepExecution.PlaceholderTask task, PriorityConfigurationCallback priorityCallback) {
        Job<?, ?> job = (Job<?, ?>) task.getOwnerTask();
        ItemInfo itemInfo = QueueItemCache.get().getItem(job.getName());
        itemInfo.getPriority();
        priorityCallback.setPrioritySelection(itemInfo.getPriority());
        return priorityCallback;
    }

    static boolean isPlaceholderTaskUsed() {
        return placeholderTaskUsed;
    }

}
