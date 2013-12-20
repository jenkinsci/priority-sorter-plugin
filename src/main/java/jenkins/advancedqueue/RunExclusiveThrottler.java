package jenkins.advancedqueue;

import hudson.Extension;
import hudson.model.Queue.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

public class RunExclusiveThrottler {

	static private List<String> exclusiveJobs = Collections.synchronizedList(new ArrayList<String>());
	static private int exclusiveJobGroupId = -1;
	static private String exclusiveJobName = "";

	@Extension
	static public class RunExclusiveRunListener extends RunListener<Run> {

		@Override
		public void onStarted(Run r, TaskListener listener) {
			ItemInfo item = QueueItemCache.get().getItem(r.getParent().getName());
			JobGroup jobGroup = PriorityConfiguration.get().getJobGroup(item.getJobGroupId());
			if (jobGroup != null && jobGroup.isRunExclusive()) {
				exclusiveJobGroupId = item.getJobGroupId();
				exclusiveJobName = item.getJobName();
				exclusiveJobs.add(item.getJobName());
			}
		}

		@Override
		public void onCompleted(Run r, TaskListener listener) {
			exclusiveJobs.remove(r.getParent().getName());
		}

	}

	private static class RunExclusiveMode extends CauseOfBlockage {

		@Override
		public String getShortDescription() {
			return "Run Exclusive (" + exclusiveJobName + ")";
		}

	}

	@Extension
	static public class RunExclusiveDispatcher extends QueueTaskDispatcher {

		@Override
		public CauseOfBlockage canRun(Item item) {
			if (exclusiveJobs.size() > 0) {
				if (QueueItemCache.get().getItem(item.id).getJobGroupId() != exclusiveJobGroupId) {
					return new RunExclusiveMode();
				}
			}
			return null;
		}

	}

}
