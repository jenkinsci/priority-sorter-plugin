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
package jenkins.advancedqueue.jobinclusion.strategy;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewGroup;
import hudson.util.ListBoxModel;

import java.util.Collection;
import java.util.regex.PatternSyntaxException;

import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
public class ViewBasedJobInclusionStrategy extends JobInclusionStrategy {

	@Extension
	static public class ViewBasedJobInclusionStrategyDescriptor extends
			AbstractJobInclusionStrategyDescriptor<ViewBasedJobInclusionStrategy> {

		public ViewBasedJobInclusionStrategyDescriptor() {
			super("Jobs included in a View");
		}

		public ListBoxModel getListViewItems() {
			ListBoxModel items = new ListBoxModel();
			Collection<View> views = Jenkins.getInstance().getViews();
			for (View view : views) {
				items.add(view.getDisplayName(), view.getViewName());
			}
			return items;
		}

	};

	static public class JobPattern {
		private String jobPattern;

		@DataBoundConstructor
		public JobPattern(String jobPattern) {
			this.jobPattern = jobPattern;
		}

	}

	private String viewName;

	private boolean useJobFilter = false;

	private String jobPattern = ".*";

	@DataBoundConstructor
	public ViewBasedJobInclusionStrategy(String viewName, JobPattern jobFilter) {
		this.viewName = viewName;
		this.useJobFilter = (jobFilter != null);
		if (this.useJobFilter) {
			this.jobPattern = jobFilter.jobPattern;
		}
	}

	public String getViewName() {
		return viewName;
	}

	public boolean isUseJobFilter() {
		return useJobFilter;
	}

	public String getJobPattern() {
		return jobPattern;
	}

	@Override
	public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job) {
		View view = Jenkins.getInstance().getView(viewName);
		if (isJobInView(job, view)) {
			if (!isUseJobFilter() || getJobPattern().trim().isEmpty()) {
				decisionLogger.addDecisionLog(2, "Not using filter ...");
				return true;
			} else {
				decisionLogger.addDecisionLog(2, "Using filter ...");
				// So filtering is on - use the priority if there's
				// a match
				try {
					if (job.getName().matches(getJobPattern())) {
						decisionLogger.addDecisionLog(3, "Job is matching the filter ...");
						return true;
					} else {
						decisionLogger.addDecisionLog(3, "Job is not matching the filter ...");
						return false;
					}
				} catch (PatternSyntaxException e) {
					// If the pattern is broken treat this a non
					// match
					decisionLogger.addDecisionLog(3, "Filter has syntax error");
					return false;
				}
			}
		}
		return false;
	}
	
	private boolean isJobInView(Job<?, ?> job, View view) {
		// First do a simple test using contains
		if(view.contains((TopLevelItem) job)) {
			return true;
		}
		// Then try to get the Items (Sectioned View)
		if(view.getItems().contains(job)) {
			return true;
		}
		// Then try to iterate over the ViewGroup (Nested View)
		if(view instanceof ViewGroup) {
			return isJobInViewGroup(job, (ViewGroup) view);
		} 
		return false;
	}
	
	private boolean isJobInViewGroup(Job<?, ?> job, ViewGroup viewGroup) {
		Collection<View> views = viewGroup.getViews();
		for (View view : views) {
			if(isJobInView(job, view)) {
				return true;
			}
		}
		return false;
	}

}
