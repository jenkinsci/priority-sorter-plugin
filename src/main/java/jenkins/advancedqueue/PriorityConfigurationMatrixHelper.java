package jenkins.advancedqueue;

import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.Job;

class PriorityConfigurationMatrixHelper {

	boolean isMatrixConfiguration(Job<?, ?> job) {
		return job instanceof MatrixConfiguration;
	}

	PriorityConfigurationCallback getPriority(MatrixConfiguration matrixConfiguration,
			PriorityConfigurationCallback priorityCallback) {
		// [JENKINS-8597]
		// For MatrixConfiguration use the latest assigned Priority from the
		// MatrixProject
		MatrixProject matrixProject = matrixConfiguration.getParent();
		priorityCallback.addDecisionLog(0, "Job is MatrixConfiguration [" + matrixProject.getName() + "] ...");
		ItemInfo itemInfo = QueueItemCache.get().getItem(matrixProject.getName());
		// Can be null (for example) at startup when the MatrixBuild got
		// lost (was running at restart)
		if (itemInfo != null) {
			priorityCallback.addDecisionLog(0, "MatrixProject found in cache, using priority from queue-item ["
					+ itemInfo.getItemId() + "]");
			return priorityCallback.setPrioritySelection(itemInfo.getPriority(), itemInfo.getJobGroupId(),
					itemInfo.getPriorityStrategy());
		}
		priorityCallback.addDecisionLog(0, "MatrixProject not found in cache, assigning global default priority");
		return priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy()
				.getDefaultPriority());

	}
}
