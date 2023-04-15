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
package jenkins.advancedqueue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.advancedqueue.JobGroup.PriorityStrategyHolder;
import jenkins.advancedqueue.priority.strategy.PriorityJobProperty;
import jenkins.advancedqueue.sorter.SorterStrategy;
import jenkins.advancedqueue.sorter.SorterStrategyDescriptor;
import jenkins.advancedqueue.sorter.strategy.AbsoluteStrategy;
import jenkins.advancedqueue.sorter.strategy.MultiBucketStrategy;
import jenkins.advancedqueue.util.PrioritySorterUtil;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class PrioritySorterConfiguration extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(PrioritySorterConfiguration.class.getName());
    private static final SorterStrategy DEFAULT_STRATEGY =
            new AbsoluteStrategy(MultiBucketStrategy.DEFAULT_PRIORITIES_NUMBER, MultiBucketStrategy.DEFAULT_PRIORITY);

    /**
     * @deprecated used in 2.x - replaces with XXX
     */
    @Deprecated
    private boolean allowPriorityOnJobs;

    private boolean onlyAdminsMayEditPriorityConfiguration = false;

    private SorterStrategy strategy;

    public PrioritySorterConfiguration() {}

    public static void init() {
        PrioritySorterConfiguration prioritySorterConfiguration = PrioritySorterConfiguration.get();
        // Make sure default is good for updating from legacy
        prioritySorterConfiguration.strategy = DEFAULT_STRATEGY; // TODO: replace with class ref
        prioritySorterConfiguration.allowPriorityOnJobs = false;
        prioritySorterConfiguration.load();
    }

    public boolean getOnlyAdminsMayEditPriorityConfiguration() {
        return onlyAdminsMayEditPriorityConfiguration;
    }

    public SorterStrategy getStrategy() {
        return strategy;
    }

    public ListBoxModel doFillStrategyItems() {
        ListBoxModel strategies = new ListBoxModel();
        List<SorterStrategyDescriptor> values = SorterStrategy.getAllSorterStrategies();
        for (SorterStrategyDescriptor sorterStrategy : values) {
            strategies.add(sorterStrategy.getDisplayName(), sorterStrategy.getKey());
        }
        return strategies;
    }

    public ListBoxModel doGetPriorityItems() {
        ListBoxModel items = PrioritySorterUtil.fillPriorityItems(strategy.getNumberOfPriorities());
        items.add(
                0,
                new ListBoxModel.Option(
                        Messages.Use_default_priority(),
                        String.valueOf(PriorityCalculationsUtil.getUseDefaultPriorityPriority())));
        return items;
    }

    public FormValidation doCheckNumberOfPriorities(@QueryParameter String value) {
        if (value.length() == 0) {
            return FormValidation.error(Messages.PrioritySorterConfiguration_enterValueRequestMessage());
        }
        try {
            int intValue = Integer.parseInt(value);
            if (intValue <= 0) {
                return FormValidation.error(Messages.PrioritySorterConfiguration_enterValueRequestMessage());
            }
        } catch (NumberFormatException e) {
            return FormValidation.error(Messages.PrioritySorterConfiguration_enterValueRequestMessage());
        }
        return FormValidation.ok();
    }

    @SuppressFBWarnings(
            value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
            justification = "try with resources checks null")
    private void updatePriorities(int prevNumberOfPriorities) {
        // Shouldn't really by a permission problem when getting here but
        // to be on the safe side
        try (ACLContext saveCtx = ACL.as(ACL.SYSTEM)) {
            @SuppressWarnings("rawtypes")
            List<Job> allJobs = Jenkins.get().getAllItems(Job.class);
            for (Job<?, ?> job : allJobs) {
                try {
                    // Scale any priority on the Job
                    PriorityJobProperty priorityProperty = job.getProperty(PriorityJobProperty.class);
                    if (priorityProperty != null && priorityProperty.getUseJobPriority()) {
                        int newPriority = PriorityCalculationsUtil.scale(
                                prevNumberOfPriorities, strategy.getNumberOfPriorities(), priorityProperty.priority);
                        if (newPriority != priorityProperty.getPriority()) {
                            job.removeProperty(priorityProperty);
                            job.addProperty(new PriorityJobProperty(priorityProperty.getUseJobPriority(), newPriority));
                            job.save();
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to update Advanced Job Priority To {0}", job.getName());
                }
            }
            //
            List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
            for (JobGroup jobGroup : jobGroups) {
                jobGroup.setPriority(PriorityCalculationsUtil.scale(
                        prevNumberOfPriorities, strategy.getNumberOfPriorities(), jobGroup.getPriority()));
                List<PriorityStrategyHolder> priorityStrategies = jobGroup.getPriorityStrategies();
                for (PriorityStrategyHolder priorityStrategyHolder : priorityStrategies) {
                    priorityStrategyHolder
                            .getPriorityStrategy()
                            .numberPrioritiesUpdates(prevNumberOfPriorities, strategy.getNumberOfPriorities());
                }
            }
            PriorityConfiguration.get().save();
        }
    }

    @DataBoundSetter
    public void setOnlyAdminsMayEditPriorityConfiguration(boolean onlyAdminsMayEditPriorityConfiguration) {
        this.onlyAdminsMayEditPriorityConfiguration = onlyAdminsMayEditPriorityConfiguration;
        save();
    }

    @DataBoundSetter
    public void setStrategy(SorterStrategy strategy) {
        updatePriorities(strategy.getNumberOfPriorities());
        this.strategy = strategy;
        save();
    }

    public static PrioritySorterConfiguration get() {
        return GlobalConfiguration.all().get(PrioritySorterConfiguration.class);
    }
}
