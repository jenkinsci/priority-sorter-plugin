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
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
public class ViewBasedJobInclusionStrategy extends JobInclusionStrategy {

    private static final Logger LOGGER = Logger.getLogger(ViewBasedJobInclusionStrategy.class.getName());

    @Extension
    public static class ViewBasedJobInclusionStrategyDescriptor
            extends AbstractJobInclusionStrategyDescriptor<ViewBasedJobInclusionStrategy> {

        public ViewBasedJobInclusionStrategyDescriptor() {
            super(Messages.Jobs_included_in_a_view());
        }

        public ListBoxModel getListViewItems() {
            ListBoxModel items = new ListBoxModel();
            Collection<View> views = Jenkins.get().getViews();
            addViews("", items, views);
            return items;
        }

        private void addViews(String parent, ListBoxModel items, Collection<View> views) {
            for (View view : views) {
                items.add(parent + view.getDisplayName(), parent + view.getViewName());
                if (view instanceof ViewGroup) {
                    addViews(parent + view.getDisplayName() + "/", items, ((ViewGroup) view).getViews());
                }
            }
        }

        public FormValidation doCheckJobPattern(@QueryParameter String jobPattern) {
            if (jobPattern.isEmpty()) {
                return FormValidation.ok(Messages.Empty_pattern_matches_all_jobs());
            }
            try {
                Pattern.compile(jobPattern);
            } catch (PatternSyntaxException exception) {
                return FormValidation.error(exception.getDescription());
            }
            return FormValidation.ok(Messages.Pattern_is_valid());
        }
    }
    ;

    public static class JobPattern {
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
    public ViewBasedJobInclusionStrategy(String viewName) {
        this.viewName = viewName;
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

    @DataBoundSetter
    public void setJobFilter(JobPattern jobFilter) {
        this.useJobFilter = (jobFilter != null);
        if (this.useJobFilter) {
            if (jobFilter != null) {
                this.jobPattern = jobFilter.jobPattern;
            } else {
                LOGGER.log(Level.SEVERE, "Ignoring null job filter for view ''{0}''", viewName);
            }
        }
    }

    @DataBoundSetter
    public void setUseJobFilter(boolean useJobFilter) {
        this.useJobFilter = useJobFilter;
    }

    @DataBoundSetter
    public void setJobPattern(String jobPattern) {
        this.jobPattern = jobPattern;
    }

    private View getView() {
        String[] nestedViewNames = this.viewName.split("/");
        final Jenkins jenkins = Jenkins.get();
        View view = jenkins.getView(nestedViewNames[0]);
        if (null == view) {
            LOGGER.log(Level.SEVERE, "Configured View does not exist ''{0}'' using primary view", viewName);
            return jenkins.getPrimaryView();
        }
        for (int i = 1; i < nestedViewNames.length; i++) {
            if (!(view instanceof ViewGroup)) {
                LOGGER.log(Level.SEVERE, "View is not a ViewGroup ''{0}'', using primary view", viewName);
                return jenkins.getPrimaryView();
            }
            view = ((ViewGroup) view).getView(nestedViewNames[i]);
            if (null == view) {
                LOGGER.log(Level.SEVERE, "Configured View does not exist ''{0}'' using primary view", viewName);
                return jenkins.getPrimaryView();
            }
        }
        return view;
    }

    @Override
    public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job) {
        if (isJobInView(job, getView())) {
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
        if (view.contains((TopLevelItem) job)) {
            return true;
        }
        // Then try to get the Items (Sectioned View)
        if (view.getItems().contains(job)) {
            return true;
        }
        // Then try to iterate over the ViewGroup (Nested View)
        if (view instanceof ViewGroup) {
            return isJobInViewGroup(job, (ViewGroup) view);
        }
        return false;
    }

    private boolean isJobInViewGroup(Job<?, ?> job, ViewGroup viewGroup) {
        Collection<View> views = viewGroup.getViews();
        for (View view : views) {
            if (isJobInView(job, view)) {
                return true;
            }
        }
        return false;
    }
}
