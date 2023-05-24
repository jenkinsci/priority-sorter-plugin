/*
 * The MIT License
 *
 * Copyright 2013 Magnus Sandberg, Oleg Nenashev
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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy;
import jenkins.advancedqueue.priority.PriorityStrategy;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Describes job group for Advanced Queue Sorter.
 *
 * @author Magnus Sandberg
 * @author Oleg Nenashev
 * @since 2.0
 */
public class JobGroup {

    public static class PriorityStrategyHolder {
        private int id = 0;
        private PriorityStrategy priorityStrategy;

        public PriorityStrategyHolder() {}

        @DataBoundConstructor
        public PriorityStrategyHolder(int id, PriorityStrategy priorityStrategy) {
            this.id = id;
            this.priorityStrategy = priorityStrategy;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public PriorityStrategy getPriorityStrategy() {
            return priorityStrategy;
        }

        public void setPriorityStrategy(PriorityStrategy priorityStrategy) {
            this.priorityStrategy = priorityStrategy;
        }
    }

    private int id = 0;
    private int priority = 2;
    /**
     * @deprecated Used in 2.x now replaced with dynamic {@link JobGroup#jobGroupStrategy}
     */
    @Deprecated
    private String view = null;

    private JobInclusionStrategy jobGroupStrategy = null;
    private String description = "";

    private boolean runExclusive = false;
    /**
     * @deprecated Used in 2.x now replaced with dynamic {@link JobGroup#jobGroupStrategy}
     */
    @Deprecated
    private boolean useJobFilter = false;
    /**
     * @deprecated Used in 2.x now replaced with dynamic {@link JobGroup#jobGroupStrategy}
     */
    @Deprecated
    private String jobPattern = ".*";

    private boolean usePriorityStrategies;
    private List<JobGroup.PriorityStrategyHolder> priorityStrategies = new ArrayList<JobGroup.PriorityStrategyHolder>();

    @DataBoundConstructor
    public JobGroup() {}

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @DataBoundSetter
    public void setId(int id) {
        this.id = id;
    }

    public @NonNull String getDescription() {
        return hudson.Util.fixNull(description);
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @return the viewName or <code>null</code> if the strategy is not {@link jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy}
     * @deprecated Used in 2.x now replaced with dynamic {@link JobGroup#jobGroupStrategy}, will return the view
     */
    @Deprecated
    @CheckForNull
    public String getView() {
        if (jobGroupStrategy instanceof ViewBasedJobInclusionStrategy) {
            return ((ViewBasedJobInclusionStrategy) jobGroupStrategy).getViewName();
        }
        return null;
    }

    /**
     * @param priority the priority to set
     */
    @DataBoundSetter
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public JobInclusionStrategy getJobGroupStrategy() {
        // Convert from 2.x
        if (jobGroupStrategy == null && view != null) {
            ViewBasedJobInclusionStrategy.JobPattern pattern = new ViewBasedJobInclusionStrategy.JobPattern(jobPattern);
            jobGroupStrategy = new ViewBasedJobInclusionStrategy(view, useJobFilter == false ? null : pattern);
        }
        return jobGroupStrategy;
    }

    @DataBoundSetter
    public void setJobGroupStrategy(JobInclusionStrategy jobGroupStrategy) {
        this.view = null;
        this.jobGroupStrategy = jobGroupStrategy;
    }

    public boolean isRunExclusive() {
        return runExclusive;
    }

    @DataBoundSetter
    public void setRunExclusive(boolean runExclusive) {
        this.runExclusive = runExclusive;
    }

    public boolean isUsePriorityStrategies() {
        return usePriorityStrategies;
    }

    @DataBoundSetter
    public void setUsePriorityStrategies(boolean usePriorityStrategies) {
        this.usePriorityStrategies = usePriorityStrategies;
    }

    public List<JobGroup.PriorityStrategyHolder> getPriorityStrategies() {
        return priorityStrategies;
    }

    @DataBoundSetter
    public void setPriorityStrategies(List<PriorityStrategy> priorityStrategies) {
        this.priorityStrategies = convertToPriorityStrategyHolder(priorityStrategies);
        if (priorityStrategies.isEmpty()) {
            this.setUsePriorityStrategies(false);
        }
    }

    private List<JobGroup.PriorityStrategyHolder> convertToPriorityStrategyHolder(
            List<PriorityStrategy> priorityStrategies) {
        List<JobGroup.PriorityStrategyHolder> priorityHolderStrategies = new ArrayList<>(priorityStrategies.size());
        for (int i = 0; i < priorityStrategies.size(); i++) {
            priorityHolderStrategies.add(new JobGroup.PriorityStrategyHolder(i, priorityStrategies.get(i)));
        }
        return priorityHolderStrategies;
    }
}
