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

import static jenkins.advancedqueue.ItemTransitionLogger.logBlockedItem;
import static jenkins.advancedqueue.ItemTransitionLogger.logBuilableItem;

import hudson.model.Queue.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.PriorityConfigurationCallback;
import jenkins.advancedqueue.priority.PriorityStrategy;

/**
 * Used to store info about a Queue.Item and related information calculated by the Plugin
 *
 * @author Magnus Sandberg
 * @since 2.3
 */
public class ItemInfo
        implements PriorityConfigurationCallback, DecisionLogger, SorterStrategyCallback, Comparable<ItemInfo> {

    private long itemId;

    private long inQueueSince;

    private Long sortAsInQueueSince = null;

    private int jobGroupId;

    private PriorityStrategy priorityStrategy;

    private String jobName;

    private float weight;

    private int priority;

    private ItemStatus itemStatus;

    private List<String> decisionLog = new ArrayList<>(10);

    ItemInfo(Item item) {
        this.itemId = item.getId();
        this.inQueueSince = item.getInQueueSince();
        this.jobName = item.task.getName();
        this.itemStatus = ItemStatus.WAITING;
    }

    public PriorityConfigurationCallback setPrioritySelection(int priority, int jobGroupId, PriorityStrategy reason) {
        this.priority = priority;
        this.jobGroupId = jobGroupId;
        this.priorityStrategy = reason;
        return this;
    }

    public PriorityConfigurationCallback setPrioritySelection(
            int priority, long sortAsInQueueSince, int jobGroupId, PriorityStrategy reason) {
        this.priority = priority;
        this.sortAsInQueueSince = sortAsInQueueSince;
        this.jobGroupId = jobGroupId;
        this.priorityStrategy = reason;
        return this;
    }

    public PriorityConfigurationCallback addDecisionLog(int indent, String log) {
        this.decisionLog.add(formatLogEntry(indent, log));
        return this;
    }

    public PriorityConfigurationCallback setPrioritySelection(int priority) {
        setPrioritySelection(priority, -1, null);
        return this;
    }

    public SorterStrategyCallback setWeightSelection(float weight) {
        this.weight = weight;
        return this;
    }

    public void setBuildable() {
        itemStatus = ItemStatus.BUILDABLE;
        logBuilableItem(this);
    }

    public void setBlocked() {
        itemStatus = ItemStatus.BLOCKED;
        logBlockedItem(this);
    }

    public long getItemId() {
        return itemId;
    }

    public long getInQueueSince() {
        return inQueueSince;
    }

    public long getSortableInQueueSince() {
        if (sortAsInQueueSince != null) {
            return sortAsInQueueSince;
        }
        return inQueueSince;
    }

    public int getJobGroupId() {
        return jobGroupId;
    }

    public PriorityStrategy getPriorityStrategy() {
        return priorityStrategy;
    }

    public String getJobName() {
        return jobName;
    }

    public float getWeight() {
        return weight;
    }

    public int getPriority() {
        return priority;
    }

    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    @Override
    public int compareTo(ItemInfo o) {
        if (this.getWeight() == o.getWeight()) {
            if (this.getSortableInQueueSince() == o.getSortableInQueueSince()) {
                return Long.compare(this.getItemId(), o.getItemId());
            }
            return Long.compare(this.getSortableInQueueSince(), o.getSortableInQueueSince());
        }
        return Float.compare(this.getWeight(), o.getWeight());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemInfo itemInfo) {
            return compareTo(itemInfo) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                itemId,
                inQueueSince,
                sortAsInQueueSince,
                jobGroupId,
                priorityStrategy,
                jobName,
                weight,
                priority,
                itemStatus,
                decisionLog);
    }

    @Override
    public String toString() {
        String reason = "<none>";
        if (priorityStrategy != null) {
            reason = priorityStrategy.getDescriptor().getDisplayName();
        }
        return "Id: %s, JobName: %s, jobGroupId: %s, reason: %s, priority: %s, weight: %s, status: %s"
                .formatted(itemId, jobName, jobGroupId, reason, priority, weight, itemStatus);
    }

    public String getDescisionLog() {
        return decisionLog.isEmpty() ? "" : String.join("\n", decisionLog) + "\n";
    }

    /**
     * Formats a log entry with appropriate indentation.
     * Uses cached format strings to avoid repeated formatting pattern construction.
     */
    private String formatLogEntry(int indent, String log) {
        int spaces = Math.max(0, (indent + 1) * 2);
        if (spaces == 0) {
            return log;
        }
        return ("%" + spaces + "s%s").formatted("", log);
    }
}
