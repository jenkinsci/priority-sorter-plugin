package jenkins.advancedqueue.testutil;

import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

@Extension
public class TestRunListener extends RunListener<Run<?,?>> {

	private final static Logger LOGGER = Logger.getLogger(TestRunListener.class.getName());

	static private LinkedHashSet<ItemInfo> actuals;
	static private ExpectedItem[] expected;

	static public void init(ExpectedItem... expected) {
		TestRunListener.expected = expected;
		actuals = new LinkedHashSet<ItemInfo>(expected.length);
	}

	@Override
	public void onStarted(Run<?, ?> r, TaskListener listener) {
		LOGGER.info("ON STARTED: " + r.getParent().getName());
		try {
			ItemInfo item = QueueItemCache.get().getItem(r.getParent().getName());
			actuals.add(item);
		} catch (Throwable e) {
			LOGGER.log(Level.INFO, "###########", e);
		}
	}

	static public void assertStartedItems() {
		Assert.assertEquals("Wrong number of started items", expected.length, actuals.size());
		int i = 0;
		for (ItemInfo actual : actuals) {
			LOGGER.info("Validating Build " + i);
			Assert.assertTrue("Job mismatch at position [" + i + "] expected <" + expected[i].getJobName() + "> was <"
					+ actual.getJobName() + ">", actual.getJobName().matches(expected[i].getJobName()));
			Assert.assertEquals("Priority mismatch at position [" + i + "]", expected[i].getPriority(), actual
					.getPriority());
					i++;
		}
	}

}
