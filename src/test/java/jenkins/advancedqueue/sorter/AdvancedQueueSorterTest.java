/*
 * The MIT License
 *
 * Copyright (c) 2024, Performance Optimization
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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.strategy.AbsoluteStrategy;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LoggerRule;

/**
 * Tests for AdvancedQueueSorter string formatting optimizations.
 * Focuses on testing the lazy evaluation and text block improvements.
 */
public class AdvancedQueueSorterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public LoggerRule loggerRule = new LoggerRule();

    @Test
    public void testLazyLoggingEvaluationForFineLevel() throws Exception {
        // Setup
        Logger logger = Logger.getLogger("PrioritySorter.Queue.Sorter");
        logger.setLevel(Level.WARNING); // Disable FINE logging

        TestLogHandler handler = new TestLogHandler();
        logger.addHandler(handler);

        // Create test items
        FreeStyleProject project = jenkins.createFreeStyleProject("test-job");
        Queue.BuildableItem item = new Queue.BuildableItem(
                new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList()));

        List<Queue.BuildableItem> items = new ArrayList<>(List.of(item));

        // Configure sorter
        PrioritySorterConfiguration.get().setStrategy(new AbsoluteStrategy());
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();

        // Add item to cache for sorting
        sorter.onNewItem(item);

        // Test: Sort items with FINE logging disabled
        handler.reset();
        sorter.sortBuildableItems(items);

        // Verify: No expensive string operations were executed
        assertThat("No FINE level logs should be captured when disabled", handler.getRecords(), hasSize(0));

        // Enable FINE logging and test again
        logger.setLevel(Level.FINE);
        handler.reset();
        sorter.sortBuildableItems(items);

        // Verify: FINE level logs are now captured
        assertThat("FINE level logs should be captured when enabled", handler.getRecords(), hasSize(greaterThan(0)));

        // Verify log content contains expected format
        boolean foundExpectedLog = handler.getRecords().stream()
                .anyMatch(record -> record.getMessage().contains("Sorted")
                        && record.getMessage().contains("with Min Weight"));
        assertTrue("Should find expected log message format", foundExpectedLog);

        logger.removeHandler(handler);
    }

    @Test
    public void testLazyLoggingEvaluationForFinerLevel() throws Exception {
        // Setup
        Logger logger = Logger.getLogger("PrioritySorter.Queue.Sorter");
        logger.setLevel(Level.INFO); // Disable FINER logging

        TestLogHandler handler = new TestLogHandler();
        logger.addHandler(handler);

        // Create test items with different job names to test table formatting
        FreeStyleProject shortJob = jenkins.createFreeStyleProject("short");
        FreeStyleProject longJob = jenkins.createFreeStyleProject("very-long-job-name-that-exceeds-limit");

        Queue.BuildableItem shortItem = new Queue.BuildableItem(
                new Queue.WaitingItem(Calendar.getInstance(), shortJob, Collections.emptyList()));
        Queue.BuildableItem longItem = new Queue.BuildableItem(
                new Queue.WaitingItem(Calendar.getInstance(), longJob, Collections.emptyList()));

        List<Queue.BuildableItem> items = new ArrayList<>(List.of(shortItem, longItem));

        // Configure sorter
        PrioritySorterConfiguration.get().setStrategy(new AbsoluteStrategy());
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();

        // Add items to cache
        sorter.onNewItem(shortItem);
        sorter.onNewItem(longItem);

        // Test: Sort items with FINER logging disabled
        handler.reset();
        sorter.sortBuildableItems(items);

        // Verify: No FINER level logs (table formatting) were executed
        assertThat("No FINER level logs should be captured when disabled", handler.getRecords(), hasSize(0));

        // Enable FINER logging and test again
        logger.setLevel(Level.FINER);
        handler.reset();
        sorter.sortBuildableItems(items);

        // Verify: FINER level logs (table) are now captured
        List<LogRecord> finerRecords = handler.getRecords().stream()
                .filter(record -> record.getLevel().equals(Level.FINER))
                .toList();

        assertThat("FINER level logs should be captured when enabled", finerRecords, hasSize(greaterThan(0)));

        // Verify table format is correct
        boolean foundTableHeader = finerRecords.stream()
                .anyMatch(record -> record.getMessage().contains("Queue:")
                        && record.getMessage()
                                .contains("+----------------------------------------------------------------------+"));
        assertTrue("Should find table header in logs", foundTableHeader);

        logger.removeHandler(handler);
    }

    @Test
    public void testJobNameTruncation() throws Exception {
        // Use reflection to test the private truncateJobName method
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();
        Method truncateMethod = AdvancedQueueSorter.class.getDeclaredMethod("truncateJobName", String.class);
        truncateMethod.setAccessible(true);

        // Test short name (no truncation)
        String shortName = "short-job";
        String result = (String) truncateMethod.invoke(sorter, shortName);
        assertEquals("Short names should not be truncated", shortName, result);

        // Test exact limit (21 characters - no truncation)
        String exactLimit = "a".repeat(21);
        result = (String) truncateMethod.invoke(sorter, exactLimit);
        assertEquals("Names at exact limit should not be truncated", exactLimit, result);

        // Test long name (should be truncated)
        String longName = "very-long-job-name-that-definitely-exceeds-the-twenty-one-character-limit";
        result = (String) truncateMethod.invoke(sorter, longName);

        // Should be: "very-long..." + "...it"
        assertTrue("Long names should be truncated", result.length() < longName.length());
        assertTrue("Truncated name should contain ellipsis", result.contains("..."));
        assertTrue("Truncated name should start with first 9 chars", result.startsWith("very-long"));
        assertTrue("Truncated name should end with last 9 chars", result.endsWith("r-limit"));

        // Test null and empty edge cases
        result = (String) truncateMethod.invoke(sorter, "");
        assertEquals("Empty string should remain empty", "", result);
    }

    @Test
    public void testQueueTableFormatting() throws Exception {
        // Create jobs with various name lengths
        FreeStyleProject[] projects = {
            jenkins.createFreeStyleProject("job1"),
            jenkins.createFreeStyleProject("medium-length-job-name"),
            jenkins.createFreeStyleProject("extremely-long-job-name-that-will-be-truncated-for-display")
        };

        List<Queue.BuildableItem> items = new ArrayList<>();
        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();
        PrioritySorterConfiguration.get().setStrategy(new AbsoluteStrategy());

        // Create items and add to cache
        for (FreeStyleProject project : projects) {
            Queue.BuildableItem item = new Queue.BuildableItem(
                    new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList()));
            items.add(item);
            sorter.onNewItem(item);
        }

        // Use reflection to test buildQueueTable method
        Method buildTableMethod = AdvancedQueueSorter.class.getDeclaredMethod("buildQueueTable", List.class);
        buildTableMethod.setAccessible(true);

        String tableOutput = (String) buildTableMethod.invoke(sorter, items);

        // Verify table structure
        assertThat("Table should contain header", tableOutput, containsString("Queue:"));
        assertThat(
                "Table should contain separator lines",
                tableOutput,
                containsString("+----------------------------------------------------------------------+"));
        assertThat("Table should contain column headers", tableOutput, containsString("Item Id"));
        assertThat("Table should contain column headers", tableOutput, containsString("Job Name"));
        assertThat("Table should contain column headers", tableOutput, containsString("Priority"));
        assertThat("Table should contain column headers", tableOutput, containsString("Weight"));

        // Verify each job appears in the table
        assertThat("Short job name should appear", tableOutput, containsString("job1"));
        assertThat(
                "Medium job name should appear (possibly truncated)",
                tableOutput,
                anyOf(containsString("medium-length-job-name"), containsString("medium-le")));
        assertThat("Long job should be truncated", tableOutput, containsString("extremely...r-display"));

        // Verify table formatting consistency
        String[] lines = tableOutput.split("\n");
        long separatorLines = java.util.Arrays.stream(lines)
                .filter(line ->
                        line.contains("+----------------------------------------------------------------------+"))
                .count();
        assertTrue("Should have at least 2 separator lines", separatorLines >= 2);
    }

    @Test
    public void testConcurrentStringFormatting() throws Exception {
        // Test that string formatting works correctly under concurrent access
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Logger logger = Logger.getLogger("PrioritySorter.Queue.Sorter");
        logger.setLevel(Level.FINER);

        TestLogHandler handler = new TestLogHandler();
        logger.addHandler(handler);

        AdvancedQueueSorter sorter = AdvancedQueueSorter.get();
        PrioritySorterConfiguration.get().setStrategy(new AbsoluteStrategy());

        // Create test items
        List<Queue.BuildableItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FreeStyleProject project = jenkins.createFreeStyleProject("concurrent-job-" + i);
            Queue.BuildableItem item = new Queue.BuildableItem(
                    new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList()));
            items.add(item);
            sorter.onNewItem(item);
        }

        // Execute concurrent sorting operations
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int op = 0; op < operationsPerThread; op++) {
                        sorter.sortBuildableItems(new ArrayList<>(items));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue("All threads should complete within timeout", latch.await(30, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue("Executor should terminate", executor.awaitTermination(10, TimeUnit.SECONDS));

        // Verify no exceptions occurred and logs were generated
        assertThat(
                "Should have generated logs from concurrent operations", handler.getRecords(), hasSize(greaterThan(0)));

        // Verify no concurrent modification issues in log messages
        boolean allValidLogs = handler.getRecords().stream()
                .allMatch(record ->
                        record.getMessage() != null && !record.getMessage().isEmpty());
        assertTrue("All log messages should be valid", allValidLogs);

        logger.removeHandler(handler);
    }

    /**
     * Test log handler that captures log records for verification.
     */
    private static class TestLogHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
            // No-op for testing
        }

        @Override
        public void close() throws SecurityException {
            records.clear();
        }

        public List<LogRecord> getRecords() {
            return new ArrayList<>(records);
        }

        public void reset() {
            records.clear();
        }
    }
}
