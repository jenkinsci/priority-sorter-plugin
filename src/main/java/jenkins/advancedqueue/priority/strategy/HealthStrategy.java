/*
 * The MIT License
 *
 * Copyright (c) 2014, Magnus Sandberg
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
import hudson.model.Job;
import hudson.model.Queue;

import jenkins.advancedqueue.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Magnus Sandberg
 * @since 2.5
 */
@Extension
public class HealthStrategy extends AbstractStaticPriorityStrategy {

	@Extension
	public static class HealthStrategyDescriptor extends AbstractStaticPriorityStrategyDescriptor {

		public HealthStrategyDescriptor() {
			super(Messages.Using_the_jobs_health());
		}

	}

	private String selection;

	private String health;

	public HealthStrategy() {}

	@DataBoundConstructor                      
	public HealthStrategy(int priority, String selection, String health) {
		setPriority(priority);
		this.selection = selection;
		this.health = health;
	}

	public String getSelection() {
		return selection;
	}

	public String getHealth() {
		return health;
	}

	@Override
	public boolean isApplicable(Queue.Item item) {
		Job<?,?> job = (Job<?,?>) item.task;
		if(!job.getBuilds().iterator().hasNext()) {
			return false;
		}
		int score = job.getBuildHealth().getScore();
		int scoreOver = 0;
		int scoreUnder = 100;
	    if("HEALTH_OVER_80".equals(health)) {
	    	scoreOver = 80;
	    	scoreUnder = 100;
	    } else if("HEALTH_61_TO_80".equals(health)) {
	    	scoreOver = 61;
	    	scoreUnder = 80;
	    } else if("HEALTH_41_TO_60".equals(health)) {
	    	scoreOver = 41;
	    	scoreUnder = 60;
	    } else if("HEALTH_21_TO_40".equals(health)) {
	    	scoreOver = 21;
	    	scoreUnder = 40;	    	
	    } else if("HEALTH_0_TO_20".equals(health)) {
	    	scoreOver = 0;
	    	scoreUnder = 20;	    	
	    }
	    if("SAME".equals(selection)) {
	    	return score >= scoreOver && score <= scoreUnder;
	    }
	    if("BETTER".equals(selection)) {
	    	return score >= scoreOver;
	    }
	    if("WORSE".equals(selection)) {
	    	return score <= scoreUnder;
	    }
	    return false;
	}

}
