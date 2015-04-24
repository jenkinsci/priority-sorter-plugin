package jenkins.advancedqueue.testutil;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

import org.junit.Assert;

@Extension
public class TestRunListener extends RunListener<Run> {

	private final static Logger LOGGER = Logger.getLogger(TestRunListener.class.getName());
			
	static private List<ItemInfo> actual;
	static private ExpectedItem[] expected;
	
	static public void init(ExpectedItem...expected) {
		TestRunListener.expected = expected;
		actual = new ArrayList<ItemInfo>(expected.length);
	}
	
	@Override
	public void onStarted(Run r, TaskListener listener) {
		LOGGER.info("ON STARTED: " + r.getParent().getName());
		try {
			ItemInfo item = QueueItemCache.get().getItem(r.getParent().getName());
			actual.add(item);
		} catch(Throwable e) {
			LOGGER.log(Level.INFO, "###########", e);			
		}
	}
	
	static public void assertStartedItems() {
		Assert.assertEquals("Wrong number of started items", expected.length, actual.size());
		for (int i = 0; i < actual.size(); i++) {
			LOGGER.info("Validating Build " + i);
			Assert.assertEquals("Job mismatch at position [" + i + "]", expected[i].getJobName(), actual.get(i).getJobName());
			Assert.assertEquals("Priority mismatch at position [" + i + "]", expected[i].getPriority(), actual.get(i).getPriority());
		}
	}
	
}
