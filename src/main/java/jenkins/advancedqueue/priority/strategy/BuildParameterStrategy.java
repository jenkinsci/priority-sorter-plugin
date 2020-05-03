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
package jenkins.advancedqueue.priority.strategy;

import hudson.Extension;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.StringParameterValue;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jenkins.advancedqueue.Messages;
import jenkins.advancedqueue.PrioritySorterConfiguration;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class BuildParameterStrategy extends AbstractDynamicPriorityStrategy {

	@Extension
	static public class BuildParameterStrategyDescriptor extends AbstractDynamicPriorityStrategyDescriptor {

		public BuildParameterStrategyDescriptor() {
			super(Messages.Use_Priority_from_Build_Parameter());
		}
	};

	private String parameterName;

	public BuildParameterStrategy() {}

	@DataBoundConstructor
	public BuildParameterStrategy(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterName() {
		return parameterName;
	}

	@CheckForNull
	private Integer getPriorityInternal(@Nonnull Queue.Item item) {
		List<ParametersAction> actions = item.getActions(ParametersAction.class);
		for (ParametersAction action : actions) {
			StringParameterValue parameterValue = (StringParameterValue) action.getParameter(parameterName);
			if (parameterValue != null) {
				String value = parameterValue.getValue();
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					// continue
				}
			}
		}
		return null;
	}

	/**
	 * Gets priority of the queue item.
	 * @param item Queue item
	 * @return Priority if it can be determined. Default priority otherwise
	 */
	public int getPriority(@Nonnull Queue.Item item) {
		final Integer p = getPriorityInternal(item);
		return p != null ? p : PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
	}

	@Override
	public boolean isApplicable(@Nonnull Queue.Item item) {
		return getPriorityInternal(item) != null;
	}
}
