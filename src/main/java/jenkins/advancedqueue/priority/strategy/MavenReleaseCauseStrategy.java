/*
 * The MIT License
 *
 * Copyright (c) 2018, Benno Markiewicz
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
package jenkins.advancedqueue.priority.strategy;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Queue.Item;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

/**
 * Uses the configured priority when the build is a Maven Release.
 * <p>
 * <p>
 * The cause is provided by the
 * <a href="https://plugins.jenkins.io/m2release/">Maven Release Plug-in</a>
 * (org.jenkins-ci.plugins.m2release:m2release:VERSION). No dependency at runtime is required to
 * support scenarios where no Maven Release Plug-in is installed, but this PrioritySorter Plug-in.
 *
 * @author Benno Markiewicz
 */
public class MavenReleaseCauseStrategy extends AbstractStaticPriorityStrategy {

	@DataBoundConstructor
	public MavenReleaseCauseStrategy(int priority) {
		this.setPriority(priority);
	}

	@Override
	public boolean isApplicable(Item item) {
		List<Cause> causes = item.getCauses();
		for (Cause cause : causes) {
			// we don't want a dependency to the Maven plugin at runtime
			if (cause.getClass().getCanonicalName().equals("org.jvnet.hudson.plugins.m2release.ReleaseCause")) {
				return true;
			}
		}
		return false;
	}

	@Extension
	public static class StrategyDescriptor extends AbstractStaticPriorityStrategyDescriptor {

		public StrategyDescriptor() {
			super("Job Triggered by Maven Release.");
		}

	}

}
