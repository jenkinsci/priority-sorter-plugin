/*
 * The MIT License
 *
 * Copyright (c) 2013, Ronny Schuetz
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import hudson.model.Run;
import hudson.model.queue.WorkUnit;

/**
 * Keeps track of the Queue.Items seen by the Sorter, but removed from the queue
 * to become jobs, for UpstreamCauseStrategy.
 *
 * @author Ronny Schuetz
 * @since 2.3
 */
public class StartedJobItemCache {

	private static final int RETENTION_COUNT = 10000;
	private static final int RETENTION_TIME_HOURS = 12;

	private static StartedJobItemCache startedJobItemCache = null;

	static {
		startedJobItemCache = new StartedJobItemCache();
	}

	public static StartedJobItemCache get() {
		return startedJobItemCache;
	}

	private static class PendingItem {
		final long startTime;
		final ItemInfo itemInfo;
		final WorkUnit workUnit;

		public PendingItem(final ItemInfo itemInfo, final WorkUnit workUnit) {
			this.startTime = System.currentTimeMillis();
			this.itemInfo = itemInfo;
			this.workUnit = workUnit;
		}
	}

	private static class StartedItem {
		final String projectName;
		final int buildNumber;

		public StartedItem(final String projectName, final int buildNumber) {
			this.projectName = projectName;
			this.buildNumber = buildNumber;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(projectName, buildNumber);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final StartedItem other = (StartedItem) obj;
			return Objects.equal(this.projectName, other.projectName) && this.buildNumber == other.buildNumber;
		}
	}

	private LinkedList<PendingItem> pendingItems = new LinkedList<PendingItem>();

	private final Cache<StartedItem, ItemInfo> startedItems = CacheBuilder.newBuilder()
			.expireAfterWrite(RETENTION_TIME_HOURS, TimeUnit.HOURS).maximumSize(RETENTION_COUNT).build();

	private StartedJobItemCache() {
	}

	/**
	 * Gets the Item for a started job, already removed from the queue
	 *
	 * @param projectName
	 *            the project name
	 * @param buildNumber
	 *            the build number
	 * @return the {@link ItemInfo} for the provided id or <code>null</code> if
	 *         projectName/buildNumber combination is unknown
	 */
	public synchronized ItemInfo getStartedItem(String projectName, int buildNumber) {
		maintainCache();
		return startedItems.getIfPresent(new StartedItem(projectName, buildNumber));
	}

	public synchronized void addItem(ItemInfo itemInfo, WorkUnit primaryWorkUnit) {
		pendingItems.addLast(new PendingItem(itemInfo, primaryWorkUnit));
		maintainCache();
	}

	private void maintainCache() {
		// Collect job information from pending items to drop WorkUnit reference

		for (final Iterator<PendingItem> it = pendingItems.iterator(); it.hasNext();) {
			final PendingItem pi = it.next();
			final Run<?, ?> run = (Run<?, ?>) pi.workUnit.getExecutable();

			if (run != null) {
				startedItems.put(new StartedItem(pi.itemInfo.getJobName(), run.getNumber()), pi.itemInfo);
				it.remove();
			}
		}

		// Cleanup pendingItems

		if (pendingItems.size() > RETENTION_COUNT) {
			pendingItems.subList(0, pendingItems.size() - RETENTION_COUNT).clear();
		}

		for (final Iterator<PendingItem> it = pendingItems.iterator(); it.hasNext();) {
			final PendingItem pi = it.next();
			if (pi.startTime < System.currentTimeMillis() - RETENTION_TIME_HOURS * 60 * 60 * 1000) {
				it.remove();
			} else {
				break;
			}
		}
	}
}
