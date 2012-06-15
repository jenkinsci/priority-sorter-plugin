/*
 * The MIT License
 *
 * Copyright (c) 2010, Brad Larson
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
package hudson.queueSorter;

import hudson.Extension;
import hudson.model.*;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.QueueSorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Extension
public class PrioritySorterQueueSorter extends QueueSorter {

	private static final class BuildableComparitor implements
			Comparator<BuildableItem> {

		public int compare(BuildableItem arg0, BuildableItem arg1) {
			// Note that we sort these backwards because we want to return
			// higher-numbered items first.
			Integer priority1 = getPriority(arg1);
			return priority1.compareTo(getPriority(arg0));
		}

		private int getPriority(BuildableItem buildable) {
			if (!(buildable.task instanceof AbstractProject)) {
				// This shouldn't happen... but just in case, let's give this
				// task a really low priority so jobs with valid priorities
				// which do work will get built first.
				return 0;
			}
			AbstractProject<?, ?> project = (AbstractProject<?, ?>) buildable.task;
			PrioritySorterJobProperty jobProperty = project.getProperty(PrioritySorterJobProperty.class);
			if (jobProperty != null) {
                                return getPriorityParamValue(jobProperty, buildable);
			} else {
				// No priority has been set for this job - use the
				// default
				return PrioritySorterDefaults.getDefault();
			}
		}
                
                private int getPriorityParamValue(PrioritySorterJobProperty jobProperty, BuildableItem buildable) {
                    try {
                        return Integer.parseInt(jobProperty.priority);
                    } catch (NumberFormatException e) {
                        return substituteParamVariable(jobProperty.priority, buildable);
                    }
                }
                
                private int substituteParamVariable(String paramName, BuildableItem buildable) {
                    ParametersAction parameters = buildable.getAction(ParametersAction.class);
                    StringParameterValue priorityParameter = (StringParameterValue)parameters.getParameter(paramName);
                    
                    if (priorityParameter == null) {
                        return getDefaultParamValue(paramName, buildable);
                    }
                    int priority  = Integer.parseInt(priorityParameter.value);
                    return priority;
                }
                
                private int getDefaultParamValue(String paramName, BuildableItem buildable) {
                    AbstractProject<?, ?> project = (AbstractProject<?, ?>) buildable.task;
                    ParametersDefinitionProperty paramDefinitions = project.getAction(ParametersDefinitionProperty.class);
                    ParameterDefinition priorityDefinition = paramDefinitions.getParameterDefinition(paramName);
                    try {
                        return Integer.parseInt(((StringParameterValue)priorityDefinition.getDefaultParameterValue()).value);
                    } catch (NumberFormatException e) {
                        return PrioritySorterDefaults.getDefault();
                    }
                }
	}

        
        
	private static final BuildableComparitor comparitor = new BuildableComparitor();

	@Override
	public void sortBuildableItems(List<BuildableItem> buildables) {
		Collections.sort(buildables, comparitor);
	}
}
