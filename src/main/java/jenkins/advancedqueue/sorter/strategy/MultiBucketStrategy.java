/*
 * The MIT License
 *
 * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
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
package jenkins.advancedqueue.sorter.strategy;

import hudson.util.ListBoxModel;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.advancedqueue.sorter.SorterStrategy;
import jenkins.advancedqueue.sorter.SorterStrategyDescriptor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Implements a strategy with multiple buckets.
 * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
 * @since 2.0
 */
public abstract class MultiBucketStrategy extends SorterStrategy {
    public static final int DEFAULT_PRIORITIES_NUMBER = 5;
    public static final int DEFAULT_PRIORITY = 3;
    
    private final int numberOfPriorities;
    private final int defaultPriority;

    public MultiBucketStrategy() {
        this(DEFAULT_PRIORITIES_NUMBER, DEFAULT_PRIORITY);
    }
    
    public MultiBucketStrategy(int numberOfPriorities, int defaultPriority) {
        this.numberOfPriorities = numberOfPriorities;
        this.defaultPriority = defaultPriority;
    }

    @Override
    public final int getNumberOfPriorities() {
        return numberOfPriorities;
    }

    @Override
    public final int getDefaultPriority() {
        return defaultPriority;
    }
    
    public abstract static class MultiBucketStrategyDescriptor extends SorterStrategyDescriptor {   
        
        public ListBoxModel doUpdateDefaultPriorityItems(
                @QueryParameter("value") String strValue) {
            int value = DEFAULT_PRIORITY;
            try {
                value = Integer.valueOf(strValue);
            } catch (NumberFormatException e) {
                // Use default value
            }
            ListBoxModel items = internalFillDefaultPriorityItems(value);
            return items;
        }
      
        private ListBoxModel internalFillDefaultPriorityItems(int value) {
            ListBoxModel items = new ListBoxModel();
            for (int i = 1; i <= value; i++) {
                items.add(String.valueOf(i));
            }
            return items;
        }

        public ListBoxModel doDefaultPriority(@QueryParameter("value") String value)
                throws IOException, ServletException {
            return doFillDefaultPriorityItems();
        }

        public ListBoxModel doFillDefaultPriorityItems() {
            //TODO: replace by dynamic retrieval
            return internalFillDefaultPriorityItems(DEFAULT_PRIORITIES_NUMBER);
        }
        
        public int getDefaultPrioritiesNumber() {
            return DEFAULT_PRIORITIES_NUMBER;
        }
        
        public int getDefaultPriority() {
            return DEFAULT_PRIORITY;
        }
    }
}
