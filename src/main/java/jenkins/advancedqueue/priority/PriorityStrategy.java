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
package jenkins.advancedqueue.priority;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import jenkins.model.Jenkins;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
public abstract class PriorityStrategy implements ExtensionPoint, Describable<PriorityStrategy> {

	/** 
	 * Method that checks if strategy can assign a priority to the provided {@link Item}
	 * 
	 * The caller garanties that the {@link Item#task} is a {@link Job}
	 * 
	 * @param item the {@link Item} to check
	 * @return <code>true</code> if the {@link PriorityStrategy} is applicable else <code>false</code>
	 */
	abstract public boolean isApplicable(Queue.Item item);

	/** 
	 * Method that that return the priority that should be used for this {@link Item}, this method is only called id
	 * {@link PriorityStrategy#isApplicable(Item)} returned true
	 * 
	 * The caller garanties that the {@link Item#task} is a {@link Job}
	 * 
	 * @param item the {@link Item} to check
	 * @return the priority to be used by the provided {@link Item}
	 */
	abstract public int getPriority(Queue.Item item);

	abstract public void numberPrioritiesUpdates(int oldNumberOfPriorities, int newNumberOfPriorities);

	public static DescriptorExtensionList<PriorityStrategy, Descriptor<PriorityStrategy>> all() {
		return Jenkins.get().getDescriptorList(PriorityStrategy.class);
	}
}
