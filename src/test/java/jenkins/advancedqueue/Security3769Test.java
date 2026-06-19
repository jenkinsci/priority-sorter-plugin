/*
 * The MIT License
 *
 * Copyright 2026 Mark Waite.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jenkins.model.Jenkins;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
public class Security3769Test {

    private JenkinsRule j;
    private JenkinsRule.WebClient wc;
    private final String ADMIN = "admin";
    private final String USER = "user";
    private final String MANAGER = "manager";
    private final String READONLY = "readonly";
    private final String SYSTEM_READONLY = "system-readonly";
    private final String MANAGER_READONLY = "manager-readonly";

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
        wc = j.createWebClient();
        wc.getOptions().setPrintContentOnFailingStatusCode(false);
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                // full access
                .grant(Jenkins.ADMINISTER)
                .everywhere()
                .to(ADMIN)

                // Read access
                .grant(Jenkins.READ)
                .everywhere()
                .to(USER)

                // Read and Manage
                .grant(Jenkins.READ)
                .everywhere()
                .to(MANAGER)
                .grant(Jenkins.MANAGE)
                .everywhere()
                .to(MANAGER)

                // Read
                .grant(Jenkins.READ)
                .everywhere()
                .to(READONLY)

                // Read and System read
                .grant(Jenkins.READ)
                .everywhere()
                .to(SYSTEM_READONLY)
                .grant(Jenkins.SYSTEM_READ)
                .everywhere()
                .to(SYSTEM_READONLY)

                // Read, Manage and System read
                .grant(Jenkins.READ)
                .everywhere()
                .to(MANAGER_READONLY)
                .grant(Jenkins.MANAGE)
                .everywhere()
                .to(MANAGER_READONLY)
                .grant(Jenkins.SYSTEM_READ)
                .everywhere()
                .to(MANAGER_READONLY));
    }

    @Test
    void jobGroupsVisibleWhenOnlyAdminsMayEditPriorityConfigurationIsTrue() throws Exception {
        PrioritySorterConfiguration globalConfig = PrioritySorterConfiguration.get();
        globalConfig.setOnlyAdminsMayEditPriorityConfiguration(true);

        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(READONLY);
        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(USER);
        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(SYSTEM_READONLY);
        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(MANAGER_READONLY);
        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(MANAGER);
        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(ADMIN);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });
    }

    @Test
    void jobGroupsVisibleWhenOnlyAdminsMayEditPriorityConfigurationIsFalse() throws Exception {
        PrioritySorterConfiguration globalConfig = PrioritySorterConfiguration.get();
        globalConfig.setOnlyAdminsMayEditPriorityConfiguration(false);

        assertThrows(FailingHttpStatusCodeException.class, () -> {
            wc.goTo("advanced-build-queue/");
        });

        wc.login(READONLY);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });

        wc.login(SYSTEM_READONLY);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });

        wc.login(USER);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });

        wc.login(MANAGER_READONLY);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });

        wc.login(MANAGER);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });

        wc.login(ADMIN);
        assertDoesNotThrow(() -> {
            HtmlPage page = wc.goTo("advanced-build-queue/");
            assertThat(page.asNormalizedText(), containsString("Job Priorities"));
        });
    }
}
