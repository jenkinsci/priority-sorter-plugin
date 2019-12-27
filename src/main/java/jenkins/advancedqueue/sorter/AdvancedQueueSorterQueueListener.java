/*
 * The MIT License
 *
 * Copyright (c) 2013, Magnus Sandberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.advancedqueue.sorter;

import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class AdvancedQueueSorterQueueListener extends QueueListener {

	private final static Logger LOGGER = Logger.getLogger(AdvancedQueueSorterQueueListener.class.getName());

	@Override
	public void onEnterWaiting(WaitingItem wi) {
		AdvancedQueueSorter.get().onNewItem(wi);
	}

	@Override
	public void onLeft(LeftItem li) {
		AdvancedQueueSorter.get().onLeft(li);
	}

	@Override
	public void onEnterBuildable(BuildableItem bi) {
		ItemInfo item = QueueItemCache.get().getItem(bi.getId());
		// Null at startup - onEnterWaiting not called during startup (?)
		if (item == null) {
			LOGGER.warning("onEnterBuilding() called without prior call to onEnterWaiting() for '" + bi.task.getDisplayName() + "'"); 
			AdvancedQueueSorter.get().onNewItem(bi);
		}
		QueueItemCache.get().getItem(bi.getId()).setBuildable();
	}

	@Override
	public void onEnterBlocked(BlockedItem bi) {
		ItemInfo item = QueueItemCache.get().getItem(bi.getId());
		// Null at startup - onEnterWaiting not called during startup (?)
		if (item == null) {
			LOGGER.warning("onEnterBlocked() called without prior call to onEnterWaiting() for '" + bi.task.getDisplayName() + "'"); 
			AdvancedQueueSorter.get().onNewItem(bi);
		}
		QueueItemCache.get().getItem(bi.getId()).setBlocked();
	}

}
