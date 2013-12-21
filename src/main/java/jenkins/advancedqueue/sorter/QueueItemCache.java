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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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

	private Map<Integer, ItemInfo> item2info = new HashMap<Integer, ItemInfo>();
	private Map<Integer, ItemInfo> weakItem2info = new WeakHashMap<Integer, ItemInfo>();
	private Map<String, ItemInfo> weakJobName2info = new WeakHashMap<String, ItemInfo>();

	private QueueItemCache() {
	}

	public ItemInfo getItem(int itemId) {
		return weakItem2info.get(itemId);
	}

	public ItemInfo getItem(String jobName) {
		return weakJobName2info.get(jobName);
	}

	public ItemInfo addItem(ItemInfo itemInfo) {
		Integer itemId = new Integer(itemInfo.getItemId());
		item2info.put(itemId, itemInfo);
		weakItem2info.put(itemId, itemInfo);
		weakJobName2info.put(new String(itemInfo.getJobName()), itemInfo);
		return itemInfo;
	}

	public ItemInfo removeItem(int itemId) {
		return item2info.remove(itemId);
	}

}
