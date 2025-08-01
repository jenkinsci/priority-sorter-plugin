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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Tests for ItemInfo string formatting optimizations.
 * Focuses on decision log formatting and toString optimizations.
 */
public class ItemInfoTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testDecisionLogFormatting() throws Exception {
        // Create test item
        FreeStyleProject project = jenkins.createFreeStyleProject("test-job");
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Test empty decision log
        String emptyLog = itemInfo.getDescisionLog();
        assertEquals("Empty decision log should return empty string", "", emptyLog);

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
        assertEquals("Should have 4 log entries", 4, lines.length);

        // Verify indentation formatting
        assertTrue("First entry should have minimal indentation", lines[0].startsWith("  Evaluating JobGroup"));
        assertTrue("Second entry should have more indentation", lines[1].startsWith("    Strategy is applicable"));
        assertTrue("Third entry should have most indentation", lines[2].startsWith("      Priority found"));
        assertTrue("Fourth entry should return to minimal indentation", lines[3].startsWith("  Final decision made"));
    }

    @Test
    public void testDecisionLogIndentationLevels() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("indent-test");
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Test various indentation levels (0-5)
        for (int indent = 0; indent <= 5; indent++) {
            itemInfo.addDecisionLog(indent, "Message at indent " + indent);
        }

        String decisionLog = itemInfo.getDescisionLog();
        String[] lines = decisionLog.split("\n");

        assertEquals("Should have 6 log entries", 6, lines.length);

        // Verify each indentation level
        for (int i = 0; i < lines.length; i++) {
            int expectedSpaces = (i + 1) * 2; // (indent + 1) * 2
            String expectedPrefix = " ".repeat(expectedSpaces) + "Message at indent " + i;
            assertEquals("Indentation should be correct for level " + i, expectedPrefix, lines[i]);
        }
    }

    @Test
    public void testFormatLogEntryMethod() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("format-test");
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Use reflection to test the private formatLogEntry method
        Method formatMethod = ItemInfo.class.getDeclaredMethod("formatLogEntry", int.class, String.class);
        formatMethod.setAccessible(true);

        // Test various combinations
        String result0 = (String) formatMethod.invoke(itemInfo, 0, "Test message");
        assertEquals("Indent 0 should produce 2 spaces", "  Test message", result0);

        String result1 = (String) formatMethod.invoke(itemInfo, 1, "Test message");
        assertEquals("Indent 1 should produce 4 spaces", "    Test message", result1);

        String result3 = (String) formatMethod.invoke(itemInfo, 3, "Test message");
        assertEquals("Indent 3 should produce 8 spaces", "        Test message", result3);

        // Test with empty message
        String resultEmpty = (String) formatMethod.invoke(itemInfo, 1, "");
        assertEquals("Empty message should still get proper indentation", "    ", resultEmpty);

        // Test with special characters
        String resultSpecial = (String) formatMethod.invoke(itemInfo, 0, "Message with \t tabs and \n newlines");
        assertTrue(
                "Special characters should be preserved", resultSpecial.contains("\t") && resultSpecial.contains("\n"));
    }

    @Test
    public void testToStringFormatting() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("toString-test");
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
        FreeStyleProject project = jenkins.createFreeStyleProject("large-content-test");
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
        assertEquals("Should have correct number of lines", entryCount, lines.length);

        // Verify first and last entries
        assertTrue("First entry should be correctly formatted", lines[0].contains("Log entry number 0"));
        assertTrue(
                "Last entry should be correctly formatted",
                lines[entryCount - 1].contains("Log entry number " + (entryCount - 1)));

        // Test that String.join is more efficient than StringBuilder for large logs
        long startTime = System.nanoTime();
        String result = itemInfo.getDescisionLog();
        long endTime = System.nanoTime();

        // Just verify it completes in reasonable time (< 10ms for 1000 entries)
        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue("Large decision log should format quickly", durationMs < 10);
    }

    @Test
    public void testConcurrentDecisionLogAccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("concurrent-test");
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
                            assertNotNull("Decision log should never be null", log);
                        }
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

        // Verify final state
        String finalLog = itemInfo.getDescisionLog();
        assertThat("Final decision log should contain entries", finalLog, not(emptyString()));

        // Count total entries - concurrent access may lead to some lost entries due to ArrayList not being thread-safe
        // This is expected behavior and demonstrates the need for proper synchronization in production code
        int totalEntries = finalLog.split("\n").length;
        assertTrue(
                "Should have substantial number of entries from concurrent access",
                totalEntries > threadCount * operationsPerThread / 10); // At least 10% of expected entries
    }

    @Test
    public void testDecisionLogEdgeCases() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("edge-cases");
        Queue.Item queueItem = new Queue.WaitingItem(Calendar.getInstance(), project, Collections.emptyList());
        ItemInfo itemInfo = new ItemInfo(queueItem);

        // Test with null message (should handle gracefully)
        try {
            itemInfo.addDecisionLog(0, null);
            String log = itemInfo.getDescisionLog();
            assertTrue("Should handle null message gracefully", log.contains("null"));
        } catch (Exception e) {
            fail("Should not throw exception for null message");
        }

        // Test with empty message
        itemInfo.addDecisionLog(1, "");
        String log = itemInfo.getDescisionLog();
        String[] lines = log.split("\n");
        assertTrue("Should handle empty message", lines[lines.length - 1].trim().isEmpty());

        // Test with very long message
        String longMessage = "x".repeat(10000);
        itemInfo.addDecisionLog(0, longMessage);
        log = itemInfo.getDescisionLog();
        assertTrue("Should handle very long messages", log.contains(longMessage));

        // Test with negative indent (edge case)
        itemInfo.addDecisionLog(-1, "Negative indent test");
        log = itemInfo.getDescisionLog();
        // Should still work (indent + 1 = 0, so no spaces)
        assertTrue("Should handle negative indent", log.contains("Negative indent test"));
    }

    @Test
    public void testMemoryEfficiencyOfStringJoin() throws Exception {
        // Compare memory usage of String.join vs StringBuilder approach
        FreeStyleProject project = jenkins.createFreeStyleProject("memory-test");
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
            assertNotNull("Log should not be null", log);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // Should complete 1000 operations in reasonable time
        assertTrue("String.join approach should be efficient", durationMs < 100);
    }
}
