package jenkins.advancedqueue;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.Queue.Item;
import hudson.model.Queue.LeftItem;
import hudson.model.listeners.RunListener;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.model.Jenkins;

public class RunExclusiveThrottler {

	static private List<String> exclusiveJobs = Collections.synchronizedList(new ArrayList<String>());
	static private int exclusiveJobGroupId = -1;
	static private String exclusiveJobName = "";

	@Extension
	static public class RunExclusiveRunListener extends RunListener<Run> {

		@Override
		public void onStarted(Run r, TaskListener listener) {
			System.out.println("Started: " + r.getParent().getName());
			ItemInfo item = QueueItemCache.get().getItem(r.getParent().getName());
			if (PriorityConfiguration.get().getJobGroup(item.getJobGroupId()).isRunExclusive()) {
				System.out.println("Is Exclusive");
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
			if(exclusiveJobs.size() > 0) {
				System.out.println(QueueItemCache.get().getItem(item.id).getJobGroupId() + " = " + exclusiveJobGroupId);
				if (QueueItemCache.get().getItem(item.id).getJobGroupId() != exclusiveJobGroupId) {
					System.out.println("dENY " + item.task.getName());
					return new RunExclusiveMode();				
				}				
			}
			System.out.println("Allow " + item.task.getName());
			return null;
		}

	}

}
