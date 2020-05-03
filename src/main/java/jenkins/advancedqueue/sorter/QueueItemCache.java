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

import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.BuildableItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;

/**
 * Keeps track of the Queue.Items seen by the Sorter. Uses a WeakHash to store the entries that have
 * left the queue, this can be used by Strategies that needs this info but still minimizes the need
 * to lookup the data again from Jenkins Core.
 * 
 * @author Magnus Sandberg
 * @since 2.3
 */
public class QueueItemCache {

	static private QueueItemCache queueItemCache = null;

	static {
		queueItemCache = new QueueItemCache();
	}

	static public QueueItemCache get() {
		return queueItemCache;
	}

	// Keeps track of all items currently in the queue
	private Map<Long, ItemInfo> item2info = new HashMap<Long, ItemInfo>();
	// Keeps track of the last started item of the Job
	private Map<String, ItemInfo> jobName2info = new HashMap<String, ItemInfo>();

	private QueueItemCache() {
	}

	/**
	 * Gets the Item for and itemId/queueId
	 * 
	 * @param itemId the id of a Job currently in the queue
	 * @return the {@link ItemInfo} for the provided id or <code>null</code> if the id is not in the
	 *         queue
	 */
	synchronized public ItemInfo getItem(long itemId) {
		return item2info.get(itemId);
	}

	/**
	 * Get the ItemInfo for the last knows start of this Job Name
	 * 
	 * @param jobName a name of a Job
	 * @return the {@link ItemInfo} for the last know start of the Job.
         *         Can be {@code null} if job didn't run yet
	 */
        @CheckForNull
	synchronized public ItemInfo getItem(String jobName) {
		return jobName2info.get(jobName);
	}

	synchronized public ItemInfo addItem(ItemInfo itemInfo) {
		Long itemId = Long.valueOf(itemInfo.getItemId());
		item2info.put(itemId, itemInfo);
		jobName2info.put(itemInfo.getJobName(), itemInfo);
		return itemInfo;
	}

        @CheckForNull
	synchronized public ItemInfo removeItem(long itemId) {
		return item2info.remove(itemId);
	}

	/**
	 * This method will return a sorted list of all known and active {@link ItemInfo}s this will
	 * include Items mapped to {@link BuildableItem}s as well as {@link BlockedItem}s
	 * 
	 * @return the sorted list of all {@link ItemInfo}s
	 */
	synchronized public List<ItemInfo> getSortedList() {
		ArrayList<ItemInfo> list = new ArrayList<ItemInfo>(item2info.values());
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}
}
