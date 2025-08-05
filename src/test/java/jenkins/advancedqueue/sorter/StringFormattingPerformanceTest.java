/*
 * The MIT License
 *
 * Copyright (c) 2025, Darin Pope
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.strategy.AbsoluteStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Performance benchmarks for string formatting optimizations.
 * These tests validate that the optimizations provide the expected performance improvements.
 */
@WithJenkins
class StringFormattingPerformanceTest {

    private static JenkinsRule jenkins;

    private Logger logger;
    private NoOpHandler handler;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) {
        jenkins = rule;
    }

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("PrioritySorter.Queue.Sorter");
        handler = new NoOpHandler();
        logger.addHandler(handler);
        PrioritySorterConfiguration.get().setStrategy(new AbsoluteStrategy());
    }

    @AfterEach
    void tearDown() {
        if (handler != null && logger != null) {
            logger.removeHandler(handler);
        }
    }

    @Test
    public void benchmarkLazyLoggingPerformance() throws Exception {

        // Create test items
        List<Queue.BuildableItem> items = createTestItems(50);
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();

        // Add items to cache
        for (Queue.BuildableItem item : items) {
            sorter.onNewItem(item);
        }

        // Benchmark with logging DISABLED (should be very fast due to lazy evaluation)
        logger.setLevel(Level.WARNING);

        long startTime = System.nanoTime();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            sorter.sortBuildableItems(new ArrayList<>(items));
        }

        long disabledTime = System.nanoTime() - startTime;
        long disabledTimeMs = disabledTime / 1_000_000;

        // Benchmark with logging ENABLED (will execute string formatting)
        logger.setLevel(Level.FINER);

        startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            sorter.sortBuildableItems(new ArrayList<>(items));
        }

        long enabledTime = System.nanoTime() - startTime;
        long enabledTimeMs = enabledTime / 1_000_000;

        // Performance assertions
        assertTrue(disabledTimeMs < 1000, "Disabled logging should complete quickly"); // < 1 second
        assertTrue(enabledTimeMs < 5000, "Enabled logging should still be reasonable"); // < 5 seconds

        // The key benefit: disabled logging should be significantly faster
        // Allowing for some variance in timing
        double ratio = (double) enabledTimeMs / Math.max(disabledTimeMs, 1);
        assertTrue(
                ratio > 2.0 || disabledTimeMs < 10, // Either 2x faster, or very fast overall
                String.format(
                        "Lazy evaluation should provide significant performance benefit when logging disabled. "
                                + "Enabled: %dms, Disabled: %dms, Ratio: %.2f",
                        enabledTimeMs, disabledTimeMs, ratio));

        System.out.println("Lazy Logging Benchmark Results:");
        System.out.printf("  Logging Disabled: %dms (%d operations)%n", disabledTimeMs, iterations);
        System.out.printf("  Logging Enabled:  %dms (%d operations)%n", enabledTimeMs, iterations);
        System.out.printf("  Performance Ratio: %.2fx%n", ratio);
    }

    @Test
    public void benchmarkDecisionLogFormatting() throws Exception {
        // Compare old StringBuilder approach vs new String.join approach
        FreeStyleProject project = jenkins.createFreeStyleProject("benchmark-job");
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());

        // Create two ItemInfo instances for comparison
        ItemInfo itemInfo1 = new ItemInfo(queueItem);
        ItemInfo itemInfo2 = new ItemInfo(queueItem);

        // Add same log entries to both
        int entryCount = 1000;
        for (int i = 0; i < entryCount; i++) {
            String logMessage = "Performance test log entry " + i + " with additional content to make it realistic";
            itemInfo1.addDecisionLog(i % 4, logMessage);
            itemInfo2.addDecisionLog(i % 4, logMessage);
        }

        // Benchmark the optimized String.join approach (current implementation)
        long startTime = System.nanoTime();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            String log = itemInfo1.getDescisionLog();
            assertNotNull(log);
        }

        long optimizedTime = System.nanoTime() - startTime;
        long optimizedTimeMs = optimizedTime / 1_000_000;

        // Simulate old StringBuilder approach for comparison
        startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            String log = simulateOldStringBuilderApproach(itemInfo2);
            assertNotNull(log);
        }

        long oldTime = System.nanoTime() - startTime;
        long oldTimeMs = oldTime / 1_000_000;

        // Performance assertions
        assertTrue(optimizedTimeMs < 1000, "Optimized approach should complete in reasonable time");
        assertTrue(oldTimeMs > 0 && optimizedTimeMs > 0, "Both approaches should work");

        // The optimized approach should be faster or at least comparable
        double improvement = (double) oldTimeMs / Math.max(optimizedTimeMs, 1);

        System.out.println("Decision Log Formatting Benchmark Results:");
        System.out.printf(
                "  Old StringBuilder: %dms (%d operations, %d entries each)%n", oldTimeMs, iterations, entryCount);
        System.out.printf(
                "  New String.join:   %dms (%d operations, %d entries each)%n",
                optimizedTimeMs, iterations, entryCount);
        System.out.printf("  Performance Improvement: %.2fx%n", improvement);

        // Should be at least as fast, ideally faster
        assertTrue(
                improvement >= 0.8, // Allow for 20% variance in timing
                String.format(
                        "New approach should be at least as fast as old approach. " + "Old: %dms, New: %dms",
                        oldTimeMs, optimizedTimeMs));
    }

    @Test
    public void benchmarkJobNameTruncation() throws Exception {
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();
        Method truncateMethod = AdvancedQueueSorter.class.getDeclaredMethod("truncateJobName", String.class);
        truncateMethod.setAccessible(true);

        // Test data with various job name lengths
        String[] jobNames = {
            "short",
            "medium-length-job-name",
            "very-long-job-name-that-definitely-exceeds-the-twenty-one-character-limit-and-needs-truncation",
            "another-extremely-long-job-name-for-testing-performance-of-string-operations"
        };

        int iterations = 100_000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            for (String jobName : jobNames) {
                String result = (String) truncateMethod.invoke(sorter, jobName);
                assertNotNull(result);
            }
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;

        // Should complete many operations quickly
        assertTrue(totalTimeMs < 1000, "Job name truncation should be fast"); // < 1 second for 400k operations

        double operationsPerMs = (iterations * jobNames.length) / (double) Math.max(totalTimeMs, 1);

        System.out.println("Job Name Truncation Benchmark Results:");
        System.out.printf("  Total operations: %d%n", (iterations * jobNames.length));
        System.out.printf("  Total time: %dms%n", totalTimeMs);
        System.out.printf("  Operations per ms: %.0f%n", operationsPerMs);

        assertTrue(operationsPerMs > 100, "Should handle many truncation operations efficiently");
    }

    @Test
    public void benchmarkQueueTableFormatting() throws Exception {
        // Test performance of the new streaming table formatting approach
        List<Queue.BuildableItem> items = createTestItems(100); // Larger queue for realistic test
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();

        // Add items to cache
        for (Queue.BuildableItem item : items) {
            sorter.onNewItem(item);
        }

        Method buildTableMethod = AdvancedQueueSorter.class.getDeclaredMethod("buildQueueTable", List.class);
        buildTableMethod.setAccessible(true);

        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            String table = (String) buildTableMethod.invoke(sorter, items);
            assertNotNull(table);
            assertTrue(table.length() > 100, "Table should contain content");
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;

        // Should format tables efficiently
        assertTrue(
                totalTimeMs < 2000, "Queue table formatting should be fast"); // < 2 seconds for 100 tables of 100 items

        double tablesPerSecond = (iterations * 1000.0) / Math.max(totalTimeMs, 1);

        System.out.println("Queue Table Formatting Benchmark Results:");
        System.out.printf("  Items per table: %d%n", items.size());
        System.out.printf("  Tables formatted: %d%n", iterations);
        System.out.printf("  Total time: %dms%n", totalTimeMs);
        System.out.printf("  Tables per second: %.1f%n", tablesPerSecond);

        assertTrue(tablesPerSecond > 10, "Should format tables at reasonable rate");
    }

    /**
     * Creates test items with varying job name lengths for benchmarking.
     */
    private List<Queue.BuildableItem> createTestItems(int count) throws Exception {
        List<Queue.BuildableItem> items = new ArrayList<>();
        String uniqueId = String.valueOf(System.nanoTime());

        for (int i = 0; i < count; i++) {
            // Create job names of varying lengths
            String jobName =
                    switch (i % 4) {
                        case 0 -> "job-" + uniqueId + "-" + i;
                        case 1 -> "medium-length-job-name-" + uniqueId + "-" + i;
                        case 2 -> "very-long-job-name-that-exceeds-normal-length-" + uniqueId + "-" + i;
                        default ->
                            "extremely-long-job-name-that-definitely-needs-truncation-for-display-purposes-" + uniqueId
                                    + "-" + i;
                    };

            FreeStyleProject project = jenkins.createFreeStyleProject(jobName);
            Queue.BuildableItem item = new Queue.BuildableItem(
                    new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList()));
            items.add(item);
        }

        return items;
    }

    /**
     * Simulates the old StringBuilder approach for comparison.
     */
    private String simulateOldStringBuilderApproach(ItemInfo itemInfo) throws Exception {
        // Use reflection to access the decision log list
        java.lang.reflect.Field field = ItemInfo.class.getDeclaredField("decisionLog");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> decisionLog = (List<String>) field.get(itemInfo);

        // Simulate old StringBuilder approach
        StringBuilder buffer = new StringBuilder();
        for (String log : decisionLog) {
            buffer.append(log).append("\n");
        }
        return buffer.toString();
    }

    /**
     * No-op log handler that discards all log records.
     * Used to measure logging overhead without I/O impact.
     */
    private static class NoOpHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            // Discard the record
        }

        @Override
        public void flush() {
            // No-op
        }

        @Override
        public void close() throws SecurityException {
            // No-op
        }
    }
}
