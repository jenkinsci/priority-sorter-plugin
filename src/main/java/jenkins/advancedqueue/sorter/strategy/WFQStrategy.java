/*
 * The MIT License
 *
 * Copyright (c) 2013, Magnus Sandberg, Oleg Nenashev and contributors
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
package jenkins.advancedqueue.sorter.strategy;

import hudson.Extension;
import jenkins.advancedqueue.strategy.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class WFQStrategy extends FQBaseStrategy {

	public WFQStrategy() {
	}

	@DataBoundConstructor
	public WFQStrategy(int numberOfPriorities, int defaultPriority) {
		super(numberOfPriorities, defaultPriority);
	}

	@Override
	float getStepSize(int priority) {
		// If WFQ a lower priority is more important than a higher priority
		// so we must step higher priorities faster than lower ones
		//
		// The step-size for the priority is dependent on its priority
		float stepSize = MIN_STEP_SIZE * (float) priority;
		return stepSize;
	}

	@Extension
	public static class DescriptorImpl extends MultiBucketStrategyDescriptor {

		@Override
		public String getDisplayName() {
			return Messages.SorterStrategy_WFQ_displayName();
		}

		@Override
		public String getShortName() {
			return Messages.SorterStrategy_WFQ_shortName();
		}
	}
}
