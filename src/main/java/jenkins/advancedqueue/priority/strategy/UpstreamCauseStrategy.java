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
package jenkins.advancedqueue.priority.strategy;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Queue;
import hudson.model.Cause.UpstreamCause;
import jenkins.advancedqueue.Messages;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.StartedJobItemCache;

/**
 * @author Magnus Sandberg
 * @since 2.3
 */
@Extension
public class UpstreamCauseStrategy extends AbstractDynamicPriorityStrategy {

	@Extension
	static public class BuildParameterStrategyDescriptor extends AbstractDynamicPriorityStrategyDescriptor {
		public BuildParameterStrategyDescriptor() {
			super(Messages.Job_triggered_by_a_upstream_build());
		}
	};

	@DataBoundConstructor
	public UpstreamCauseStrategy() {
	}

	@CheckForNull
	private UpstreamCause getUpstreamCause(@Nonnull Queue.Item item) {
		List<Cause> causes = item.getCauses();
		for (Cause cause : causes) {
			if (cause.getClass() == UpstreamCause.class) {
				return (UpstreamCause) cause;
			}
		}
		return null;
	}

	public int getPriority(Queue.Item item) {
		UpstreamCause upstreamCause = getUpstreamCause(item);
                if (upstreamCause == null) {
                    // Cannot determine
                    return PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
                }
                
		String upstreamProject = upstreamCause.getUpstreamProject();
		int upstreamBuildId = upstreamCause.getUpstreamBuild();
		ItemInfo upstreamItem = StartedJobItemCache.get().getStartedItem(upstreamProject, upstreamBuildId);
		// Upstream Item being null should be very very rare
		if (upstreamItem != null) {
			return upstreamItem.getPriority();
		}
		return PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
	}

	@Override
	public boolean isApplicable(Queue.Item item) {
		return getUpstreamCause(item) != null;
	}
}
