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

import static jenkins.advancedqueue.ItemTransitionLogger.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Item;
import hudson.model.Queue.LeftItem;
import hudson.model.queue.QueueSorter;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.advancedqueue.PriorityConfiguration;
import jenkins.advancedqueue.PrioritySorterConfiguration;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class AdvancedQueueSorter extends QueueSorter {

    private static final Logger LOGGER = Logger.getLogger("PrioritySorter.Queue.Sorter");

    public AdvancedQueueSorter() {}

    public static void init() {
        List<BuildableItem> items = Queue.getInstance().getBuildableItems();
        // Sort the queue in the order the items entered the queue
        // so that onNewItem() happens in the correct order below
        items.sort(Comparator.comparingLong(BuildableItem::getInQueueSince));
        AdvancedQueueSorter advancedQueueSorter = AdvancedQueueSorter.get();
        for (BuildableItem item : items) {
            advancedQueueSorter.onNewItem(item);
            // Listener called before we get here so make sure we mark buildable
            ItemInfo info = QueueItemCache.get().getItem(item.getId());
            if (info != null) {
                info.setBuildable();
            }
        }
        LOGGER.log(Level.INFO, "Initialized the QueueSorter with {0} Buildable Items", items.size());
    }

    public void sortNotWaitingItems(List<? extends Queue.NotWaitingItem> items) {
        items.sort(this::compareNotWaitingItems);
        //
        if (!items.isEmpty() && LOGGER.isLoggable(Level.FINE)) {
            ItemInfo minItem = QueueItemCache.get().getItem(items.get(0).getId());
            ItemInfo maxItem =
                    QueueItemCache.get().getItem(items.get(items.size() - 1).getId());
            float minWeight = minItem != null ? minItem.getWeight() : 0;
            float maxWeight = maxItem != null ? maxItem.getWeight() : 0;
            LOGGER.log(Level.FINE, "Sorted {0} {1}s with Min Weight {2} and Max Weight {3}", new Object[] {
                items.size(), items.get(0).getClass().getName(), minWeight, maxWeight
            });
        }
        //
        if (!items.isEmpty() && LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, buildQueueTable(items));
        }
    }

    private int compareNotWaitingItems(Queue.NotWaitingItem o1, Queue.NotWaitingItem o2) {
        var item1 = QueueItemCache.get().getItem(o1.getId());
        var item2 = QueueItemCache.get().getItem(o2.getId());

        return switch (item1 == null ? 1 : item2 == null ? 2 : 0) {
            case 1, 2 -> {
                LOGGER.warning("Requested to sort unknown items, sorting on queue-time only.");
                yield Long.compare(o1.getInQueueSince(), o2.getInQueueSince());
            }
            default -> item1.compareTo(item2);
        };
    }

    @Override
    public void sortBuildableItems(List<BuildableItem> items) {
        sortNotWaitingItems(items);
    }

    @Override
    public void sortBlockedItems(List<Queue.BlockedItem> blockedItems) {
        sortNotWaitingItems(blockedItems);
    }

    public void onNewItem(@NonNull Item item) {
        final SorterStrategy prioritySorterStrategy =
                PrioritySorterConfiguration.get().getStrategy();
        ItemInfo itemInfo = new ItemInfo(item);
        PriorityConfiguration.get().getPriority(item, itemInfo);
        prioritySorterStrategy.onNewItem(item, itemInfo);
        QueueItemCache.get().addItem(itemInfo);
        logNewItem(itemInfo);
    }

    public void onLeft(@NonNull LeftItem li) {
        ItemInfo itemInfo = QueueItemCache.get().removeItem(li.getId());
        if (itemInfo == null) {
            LOGGER.log(
                    Level.WARNING,
                    "Received the onLeft() notification for the item from outside the QueueItemCache: {0}. "
                            + "Cannot process this item, Priority Sorter Strategy will not be invoked",
                    li);
            return;
        }

        final SorterStrategy prioritySorterStrategy =
                PrioritySorterConfiguration.get().getStrategy();
        if (li.isCancelled()) {
            prioritySorterStrategy.onCanceledItem(li);
            logCanceledItem(itemInfo);
        } else {
            float weight = itemInfo.getWeight();
            StartedJobItemCache.get().addItem(itemInfo, li.outcome.getPrimaryWorkUnit());
            prioritySorterStrategy.onStartedItem(li, weight);
            logStartedItem(itemInfo);
        }
    }

    public static AdvancedQueueSorter get() {
        return QueueSorter.all().get(AdvancedQueueSorter.class);
    }

    /**
     * Builds a formatted table showing queue details for logging.
     * Uses lazy evaluation - only called when FINER logging is enabled.
     */
    private String buildQueueTable(List<? extends Queue.NotWaitingItem> items) {
        var header = "%s Queue:%n+----------------------------------------------------------------------+%n"
                + "|   Item Id  |        Job Name       | Priority |        Weight        |%n"
                + "+----------------------------------------------------------------------+%n";

        var formattedHeader = header.formatted(items.get(0).getClass().getName());
        var tableRows = items.stream().map(this::formatQueueItem).collect(Collectors.joining());

        return formattedHeader + tableRows + "+----------------------------------------------------------------------+";
    }

    /**
     * Formats a single queue item for the table display.
     * Truncates long job names to fit the table format.
     */
    private String formatQueueItem(Queue.NotWaitingItem item) {
        ItemInfo itemInfo = QueueItemCache.get().getItem(item.getId());
        String itemName = itemInfo != null ? itemInfo.getJobName() : item.task.getName();
        int itemPriority = itemInfo != null ? itemInfo.getPriority() : 0;
        float itemWeight = itemInfo != null ? itemInfo.getWeight() : 0;
        String jobName = truncateJobName(itemName);
        return "| %10d | %20s | %8d | %20.5f |%n".formatted(item.getId(), jobName, itemPriority, itemWeight);
    }

    /**
     * Truncates job names that are too long for the table format.
     * Shows first 9 and last 9 characters with "..." in between.
     */
    private String truncateJobName(String jobName) {
        return jobName.length() > 21
                ? "%s...%s".formatted(jobName.substring(0, 9), jobName.substring(jobName.length() - 9))
                : jobName;
    }
}
