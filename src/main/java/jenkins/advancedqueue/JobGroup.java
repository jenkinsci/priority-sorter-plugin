/*
 * The MIT License
 *
 * Copyright 2013 Magnus Sandberg, Oleg Nenashev
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.advancedqueue.jobinclusion.strategy.ViewBasedJobInclusionStrategy;
import jenkins.advancedqueue.priority.PriorityStrategy;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Describes job group for Advanced Queue Sorter.
 * 
 * @author Magnus Sandberg
 * @author Oleg Nenashev
 * @since 2.0
 */
public class JobGroup {

	public static class PriorityStrategyHolder {
		private int id = 0;
		private PriorityStrategy priorityStrategy;

		public PriorityStrategyHolder() {
		}

		@DataBoundConstructor
		public PriorityStrategyHolder(int id, PriorityStrategy priorityStrategy) {
			this.id = id;
			this.priorityStrategy = priorityStrategy;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public PriorityStrategy getPriorityStrategy() {
			return priorityStrategy;
		}

		public void setPriorityStrategy(PriorityStrategy priorityStrategy) {
			this.priorityStrategy = priorityStrategy;
		}

	}

	private int id = 0;
	private int priority = 2;
	/**
	 * @deprecated Used 2.6 and earlier now replaced with dynamic {@link JobGroup#jobGroupStrategy}
	 */
	@Deprecated
	private String view = null;
	private JobInclusionStrategy jobGroupStrategy = null;
	private boolean runExclusive = false;
	/**
	 * @deprecated Used 2.6 and earlier now replaced with dynamic {@link JobGroup#jobGroupStrategy}
	 */
	@Deprecated
	private boolean useJobFilter = false;
	/**
	 * @deprecated Used 2.6 and earlier now replaced with dynamic {@link JobGroup#jobGroupStrategy}
	 */
	@Deprecated
	private String jobPattern = ".*";
	private boolean usePriorityStrategies;
	private List<JobGroup.PriorityStrategyHolder> priorityStrategies = new ArrayList<JobGroup.PriorityStrategyHolder>();

	private JobGroup() {
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
		
	public JobInclusionStrategy getJobGroupStrategy() {
		// Convert from 2.6 and earlier
		if(jobGroupStrategy == null && view != null) {
			ViewBasedJobInclusionStrategy.JobPattern pattern = new ViewBasedJobInclusionStrategy.JobPattern(jobPattern);
			jobGroupStrategy = new ViewBasedJobInclusionStrategy(view, useJobFilter == false ? null : pattern);
		}
		return jobGroupStrategy;
	}

	public void setJobGroupStrategy(JobInclusionStrategy jobGroupStrategy) {
		this.view = null;
		this.jobGroupStrategy = jobGroupStrategy;
	}

	public boolean isRunExclusive() {
		return runExclusive;
	}

	public void setRunExclusive(boolean runExclusive) {
		this.runExclusive = runExclusive;
	}

	public boolean isUsePriorityStrategies() {
		return usePriorityStrategies;
	}

	public void setUsePriorityStrategies(boolean usePriorityStrategies) {
		this.usePriorityStrategies = usePriorityStrategies;
	}

	public List<JobGroup.PriorityStrategyHolder> getPriorityStrategies() {
		return priorityStrategies;
	}

	public void setPriorityStrategies(List<JobGroup.PriorityStrategyHolder> priorityStrategies) {
		this.priorityStrategies = priorityStrategies;
	}

	/**
	 * Creates a Job Group from JSON object.
	 * 
	 * @param jobGroupObject JSON object with class description
	 * @param id ID of the item to be created
	 * @return created group
	 */
	// TODO: replace by DataBound Constructor
	public static JobGroup newInstance(StaplerRequest req, JSONObject jobGroupObject, int id) {
		JobGroup jobGroup = new JobGroup();
		jobGroup.setId(id);
		jobGroup.setPriority(jobGroupObject.getInt("priority"));
		JSONObject jsonObjectJobGroupStrategy = jobGroupObject.getJSONObject("jobGroupStrategy");
		JobInclusionStrategy jobGroupStrategy = req.bindJSON(Class.class, JobInclusionStrategy.class, jsonObjectJobGroupStrategy);
		jobGroup.setJobGroupStrategy(jobGroupStrategy);
		jobGroup.setRunExclusive(Boolean.parseBoolean(jobGroupObject.getString("runExclusive")));
		/*
		jobGroup.setUseJobFilter(jobGroupObject.has("useJobFilter"));
		if (jobGroup.isUseJobFilter()) {
			JSONObject jsonObject = jobGroupObject.getJSONObject("useJobFilter");
			jobGroup.setJobPattern(jsonObject.getString("jobPattern"));
			// Disable the filter if the pattern is invalid
			try {
				Pattern.compile(jobGroup.getJobPattern());
			} catch (PatternSyntaxException e) {
				jobGroup.setUseJobFilter(false);
			}
		}
		*/
		//
		jobGroup.setUsePriorityStrategies(jobGroupObject.has("usePriorityStrategies"));
		if (jobGroup.isUsePriorityStrategies()) {
			JSONObject jsonObject = jobGroupObject.getJSONObject("usePriorityStrategies");
			if (jsonObject.has("holder")) {
				JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("holder"));
				int psid = 0;
				for (Object object : jsonArray) {
					PriorityStrategyHolder holder = new JobGroup.PriorityStrategyHolder();
					holder.setId(psid++);
					PriorityStrategy strategy = req.bindJSON(Class.class, PriorityStrategy.class, object);
					holder.setPriorityStrategy(strategy);
					jobGroup.priorityStrategies.add(holder);
				}
			}
			if (jobGroup.priorityStrategies.isEmpty()) {
				jobGroup.setUsePriorityStrategies(false);
			}
		}
		return jobGroup;
	}
}
