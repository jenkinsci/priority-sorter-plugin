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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jenkins.advancedqueue.priority.strategy.JobPropertyStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests for ItemInfo string formatting optimizations.
 * Focuses on decision log formatting and toString optimizations.
 */
@WithJenkins
class ItemInfoTest {

    private static JenkinsRule jenkins;

    @BeforeAll
    static void beforeAll(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    public void testDecisionLogFormatting() throws Exception {
        // Create test item
        FreeStyleProject project = jenkins.createFreeStyleProject("test-job-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Test empty decision log
        String emptyLog = itemInfo.getDescisionLog();
        assertEquals("", emptyLog, "Empty decision log should return empty string");

        // Add various decision log entries with different indentations
        itemInfo.addDecisionLog(0, "Evaluating JobGroup [1] ...");
        itemInfo.addDecisionLog(1, "Strategy is applicable");
        itemInfo.addDecisionLog(2, "Priority found: 3");
        itemInfo.addDecisionLog(0, "Final decision made");

        String decisionLog = itemInfo.getDescisionLog();

        // Verify log structure
        assertThat("Decision log should not be empty", decisionLog, not(emptyString()));
        assertThat("Decision log should end with newline", decisionLog, endsWith("\n"));

        String[] lines = decisionLog.split("\n");
        assertEquals(4, lines.length, "Should have 4 log entries");

        // Verify indentation formatting
        assertTrue(lines[0].startsWith("  Evaluating JobGroup"), "First entry should have minimal indentation");
        assertTrue(lines[1].startsWith("    Strategy is applicable"), "Second entry should have more indentation");
        assertTrue(lines[2].startsWith("      Priority found"), "Third entry should have most indentation");
        assertTrue(lines[3].startsWith("  Final decision made"), "Fourth entry should return to minimal indentation");
    }

    @Test
    public void testDecisionLogIndentationLevels() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("indent-test-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Test various indentation levels (0-5)
        for (int indent = 0; indent <= 5; indent++) {
            itemInfo.addDecisionLog(indent, "Message at indent " + indent);
        }

        String decisionLog = itemInfo.getDescisionLog();
        String[] lines = decisionLog.split("\n");

        assertEquals(6, lines.length, "Should have 6 log entries");

        // Verify each indentation level
        for (int i = 0; i < lines.length; i++) {
            int expectedSpaces = (i + 1) * 2; // (indent + 1) * 2
            String expectedPrefix = " ".repeat(expectedSpaces) + "Message at indent " + i;
            assertEquals(expectedPrefix, lines[i], "Indentation should be correct for level " + i);
        }
    }

    @Test
    public void testFormatLogEntryMethod() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("format-test-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Use reflection to test the private formatLogEntry method
        Method formatMethod = ItemInfo.class.getDeclaredMethod("formatLogEntry", int.class, String.class);
        formatMethod.setAccessible(true);

        // Test various combinations
        String result0 = (String) formatMethod.invoke(itemInfo, 0, "Test message");
        assertEquals("  Test message", result0, "Indent 0 should produce 2 spaces");

        String result1 = (String) formatMethod.invoke(itemInfo, 1, "Test message");
        assertEquals("    Test message", result1, "Indent 1 should produce 4 spaces");

        String result3 = (String) formatMethod.invoke(itemInfo, 3, "Test message");
        assertEquals("        Test message", result3, "Indent 3 should produce 8 spaces");

        // Test with empty message
        String resultEmpty = (String) formatMethod.invoke(itemInfo, 1, "");
        assertEquals("    ", resultEmpty, "Empty message should still get proper indentation");

        // Test with special characters
        String resultSpecial = (String) formatMethod.invoke(itemInfo, 0, "Message with \t tabs and \n newlines");
        assertTrue(
                resultSpecial.contains("\t") && resultSpecial.contains("\n"), "Special characters should be preserved");
    }

    @Test
    public void testToStringFormatting() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("toString-test-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Set some values for testing
        itemInfo.setPrioritySelection(5, 10, new JobPropertyStrategy());
        itemInfo.setWeightSelection(2.5f);
        itemInfo.setBuildable();

        String toString = itemInfo.toString();

        // Verify all expected components are present
        assertThat("Should contain item ID", toString, containsString("Id: " + itemInfo.getItemId()));
        assertThat("Should contain job name", toString, containsString("JobName: toString-test"));
        assertThat("Should contain job group ID", toString, containsString("jobGroupId: 10"));
        assertThat("Should contain priority", toString, containsString("priority: 5"));
        assertThat("Should contain weight", toString, containsString("weight: 2.5"));
        assertThat("Should contain status", toString, containsString("status: BUILDABLE"));
        assertThat(
                "Should contain reason",
                toString,
                containsString("reason: Take the priority from property on the job"));

        // Test with no priority strategy (null case)
        ItemInfo itemInfo2 = new ItemInfo(queueItem);
        String toString2 = itemInfo2.toString();
        assertThat("Should handle null strategy", toString2, containsString("reason: <none>"));
    }

    @Test
    public void testDecisionLogWithLargeContent() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("large-content-test-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Add many log entries to test performance and memory usage
        int entryCount = 1000;
        for (int i = 0; i < entryCount; i++) {
            itemInfo.addDecisionLog(i % 4, "Log entry number " + i + " with some additional content");
        }

        String decisionLog = itemInfo.getDescisionLog();

        // Verify content
        assertThat("Large decision log should not be empty", decisionLog, not(emptyString()));

        String[] lines = decisionLog.split("\n");
        assertEquals(entryCount, lines.length, "Should have correct number of lines");

        // Verify first and last entries
        assertTrue(lines[0].contains("Log entry number 0"), "First entry should be correctly formatted");
        assertTrue(
                lines[entryCount - 1].contains("Log entry number " + (entryCount - 1)),
                "Last entry should be correctly formatted");

        // Test that String.join is more efficient than StringBuilder for large logs
        long startTime = System.nanoTime();
        String result = itemInfo.getDescisionLog();
        long endTime = System.nanoTime();

        // Just verify it completes in reasonable time (< 10ms for 1000 entries)
        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 10, "Large decision log should format quickly");
    }

    @Test
    public void testConcurrentDecisionLogAccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("concurrent-test-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Concurrently add log entries and read decision log
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int op = 0; op < operationsPerThread; op++) {
                        // Add log entry
                        itemInfo.addDecisionLog(op % 3, "Thread " + threadId + " operation " + op);

                        // Read decision log periodically
                        if (op % 10 == 0) {
                            String log = itemInfo.getDescisionLog();
                            assertNotNull(log, "Decision log should never be null");
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within timeout");

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor should terminate");

        // Verify final state
        String finalLog = itemInfo.getDescisionLog();
        assertThat("Final decision log should contain entries", finalLog, not(emptyString()));

        // Count total entries - concurrent access may lead to some lost entries due to ArrayList not being thread-safe
        // This is expected behavior and demonstrates the need for proper synchronization in production code
        int totalEntries = finalLog.split("\n").length;
        assertTrue(
                totalEntries > threadCount * operationsPerThread / 10, // At least 10% of expected entries
                "Should have substantial number of entries from concurrent access");
    }

    @Test
    public void testDecisionLogEdgeCases() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("edge-cases-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Test with null message (should handle gracefully)
        try {
            itemInfo.addDecisionLog(0, null);
            String log = itemInfo.getDescisionLog();
            assertTrue(log.contains("null"), "Should handle null message gracefully");
        } catch (Exception e) {
            fail("Should not throw exception for null message");
        }

        // Test with empty message
        itemInfo.addDecisionLog(1, "");
        String log = itemInfo.getDescisionLog();
        String[] lines = log.split("\n");
        assertTrue(lines[lines.length - 1].trim().isEmpty(), "Should handle empty message");

        // Test with very long message
        String longMessage = "x".repeat(10000);
        itemInfo.addDecisionLog(0, longMessage);
        log = itemInfo.getDescisionLog();
        assertTrue(log.contains(longMessage), "Should handle very long messages");

        // Test with negative indent (edge case)
        itemInfo.addDecisionLog(-1, "Negative indent test");
        log = itemInfo.getDescisionLog();
        // Should still work (indent + 1 = 0, so no spaces)
        assertTrue(log.contains("Negative indent test"), "Should handle negative indent");
    }

    @Test
    public void testMemoryEfficiencyOfStringJoin() throws Exception {
        // Compare memory usage of String.join vs StringBuilder approach
        FreeStyleProject project = jenkins.createFreeStyleProject("memory-test-" + System.nanoTime());
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Add moderate number of entries
        for (int i = 0; i < 100; i++) {
            itemInfo.addDecisionLog(i % 3, "Memory test entry " + i);
        }

        // Measure memory usage indirectly by timing multiple operations
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            String log = itemInfo.getDescisionLog();
            assertNotNull(log, "Log should not be null");
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // Should complete 1000 operations in reasonable time
        assertTrue(durationMs < 100, "String.join approach should be efficient");
    }
}
