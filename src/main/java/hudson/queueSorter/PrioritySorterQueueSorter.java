/*
 * The MIT License
 *
 * Copyright (c) 2010, Brad Larson
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
package hudson.queueSorter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.QueueSorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Extension
public class PrioritySorterQueueSorter extends QueueSorter {

	private static final class BuildableComparitor implements
			Comparator<BuildableItem> {

		public int compare(BuildableItem arg0, BuildableItem arg1) {
			// Note that we sort these backwards because we want to return
			// higher-numbered items first.
			Integer priority1 = getPriority(arg1);
			return priority1.compareTo(getPriority(arg0));
		}

		private static int getPriority(BuildableItem buildable) {
			if (!(buildable.task instanceof AbstractProject)) {
				// This shouldn't happen... but just in case, let's give this
				// task a really low priority so jobs with valid priorities
				// which do work will get built first.
				return 0;
			}
			AbstractProject<?, ?> project = (AbstractProject<?, ?>) buildable.task;
			PrioritySorterJobProperty priority = project
					.getProperty(PrioritySorterJobProperty.class);
			if (priority != null) {
				return priority.priority;
			} else {
				// No priority has been set for this job - use the default (from
				// config.jelly)
				return 100;
			}
		}
	}

	private static final BuildableComparitor comparitor = new BuildableComparitor();

	@Override
	public void sortBuildableItems(List<BuildableItem> buildables) {
		Collections.sort(buildables, comparitor);
	}
}
