package jenkins.advancedqueue;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.util.LinkedList;
import java.util.List;

import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

import org.junit.Assert;

@Extension
public class TestRunListener extends RunListener<Run> {

	static List<ItemInfo> recording = new LinkedList<ItemInfo>();

	@Override
	public void onStarted(Run r, TaskListener listener) {
		ItemInfo item = QueueItemCache.get().getItem(r.getParent().getName());
		recording.add(item);
	}

	static void assertJobStartOrder(String... names) {
		for (int i = 0; i < names.length; i++) {
			ItemInfo itemInfo = recording.get(i);
			Assert.assertEquals(itemInfo.getJobName(), names[i]);
		}
	}

}
