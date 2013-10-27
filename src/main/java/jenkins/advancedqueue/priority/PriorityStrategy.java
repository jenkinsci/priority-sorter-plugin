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

import hudson.ExtensionList;
import hudson.model.Queue$Item;
import jenkins.model.Jenkins;

import org.apache.tools.ant.ExtensionPoint;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
public abstract class PriorityStrategy extends ExtensionPoint {

	abstract public String getDisplayName();
	
	abstract public String getKey();

	abstract public boolean isApplicable(Queue$Item item);

	
	public static PriorityStrategy getStrategyFromKey(String key) {
		ExtensionList<PriorityStrategy> all = all();
		for (PriorityStrategy priorityStrategy : all) {
			if(key.equals(priorityStrategy.getKey())) {
				return priorityStrategy;
			}
		}
		return null;
	}
	/**
     * All registered {@link PriorityStrategy}s.
     */
    public static ExtensionList<PriorityStrategy> all() {
        return Jenkins.getInstance().getExtensionList(PriorityStrategy.class);
    }

}
