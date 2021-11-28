/*
 * The MIT License
 *
 * Copyright (c) 2014, Magnus Sandberg
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
package jenkins.advancedqueue.jobinclusion;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import jenkins.advancedqueue.DecisionLogger;
import jenkins.model.Jenkins;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
public abstract class JobInclusionStrategy implements ExtensionPoint, Describable<JobInclusionStrategy> {

	static public class AbstractJobInclusionStrategyDescriptor<T extends JobInclusionStrategy> extends Descriptor<JobInclusionStrategy> {

		private final String displayName;

		protected AbstractJobInclusionStrategyDescriptor(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

	};

	@SuppressWarnings("unchecked")
	public Descriptor<JobInclusionStrategy> getDescriptor() {
		return Jenkins.get().getDescriptor(this.getClass());
	}

	abstract public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job);

	public static DescriptorExtensionList<JobInclusionStrategy, Descriptor<JobInclusionStrategy>> all() {
		return Jenkins.get().getDescriptorList(JobInclusionStrategy.class);
	}

}
