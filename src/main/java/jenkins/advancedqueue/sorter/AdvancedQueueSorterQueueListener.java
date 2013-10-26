package jenkins.advancedqueue.sorter;

import hudson.Extension;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;

@Extension
public class AdvancedQueueSorterQueueListener extends QueueListener {

	@Override
    public void onEnterWaiting(WaitingItem wi) {
 		AdvancedQueueSorter.get().onEnterWaiting(wi);
	}

	@Override
	public void onLeft(LeftItem li) {
		AdvancedQueueSorter.get().onLeft(li);		
	}

}
