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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Describes job group for Advanced Queue Sorter.
 * @author Magnus Sandberg
 * @author Oleg Nenashev
 * @since 2.0
 */
public class JobGroup {
	
	public static class PriorityStrategy {
		private int id = 0;	
		private int priority = 2;
		private String priorityStrategyKey = null;

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}
		public String getPriorityStrategyKey() {
			return priorityStrategyKey;
		}
		public void setPriorityStrategyKey(String priorityStrategyKey) {
			this.priorityStrategyKey = priorityStrategyKey;
		}
	}

	private int id = 0;
	private int priority = 2;
	private String view;
	private boolean useJobFilter = false;
	private String jobPattern = ".*";
	private boolean usePriorityStrategies;
	private List<JobGroup.PriorityStrategy> priorityStrategies = new ArrayList<JobGroup.PriorityStrategy>();
	
        private JobGroup() {};
        
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

        /**
         * @return the view
         */
        public String getView() {
                return view;
        }

        /**
         * @param view the view to set
         */
        public void setView(String view) {
                this.view = view;
        }

        /**
         * @return the useJobFilter
         */
        public boolean isUseJobFilter() {
                return useJobFilter;
        }

        /**
         * @param useJobFilter the useJobFilter to set
         */
        public void setUseJobFilter(boolean useJobFilter) {
                this.useJobFilter = useJobFilter;
        }

        /**
         * @return the jobPattern
         */
        public String getJobPattern() {
                return jobPattern;
        }

        /**
         * @param jobPattern the jobPattern to set
         */
        public void setJobPattern(String jobPattern) {
                this.jobPattern = jobPattern;
        }
        

		public boolean isUsePriorityStrategies() {
			return usePriorityStrategies;
		}

		public void setUsePriorityStrategies(boolean usePriorityStrategies) {
			this.usePriorityStrategies = usePriorityStrategies;
		}

		public List<JobGroup.PriorityStrategy> getPriorityStrategies() {
			return priorityStrategies;
		}

		public void setPriorityStrategies(
				List<JobGroup.PriorityStrategy> priorityStrategies) {
			this.priorityStrategies = priorityStrategies;
		}
		
		/**
         * Creates a Job Group from JSON object.
         * @param jobGroupObject JSON object with class description
         * @param id ID of the item to be created
         * @return created group
         */
        //TODO: replace by DataBound Constructor
        public static JobGroup Create(JSONObject jobGroupObject, int id) {
            JobGroup jobGroup = new JobGroup();
            jobGroup.setId(id);
            jobGroup.setPriority(jobGroupObject.getInt("priority"));
            jobGroup.setView(jobGroupObject.getString("view"));
            jobGroup.setUseJobFilter(jobGroupObject.has("useJobFilter"));
            if(jobGroup.isUseJobFilter()) {
                    JSONObject jsonObject = jobGroupObject.getJSONObject("useJobFilter");
                    jobGroup.setJobPattern(jsonObject.getString("jobPattern"));
                    // Disable the filter if the pattern is invalid
                    try {
                            Pattern.compile(jobGroup.getJobPattern());
                    } catch (PatternSyntaxException e) {
                            jobGroup.setUseJobFilter(false);		
                    }
            }
            //
            jobGroup.setUsePriorityStrategies(jobGroupObject.has("usePriorityStrategies"));
            if(jobGroup.isUsePriorityStrategies()) {
        		JSONObject jsonObject = jobGroupObject.getJSONObject("usePriorityStrategies");
        		JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("priorityStrategy"));
        		int psid = 0;
        		for (Object object : jsonArray) {
        			JSONObject psObject = JSONObject.fromObject(object);
        			System.out.println(psObject);
        			if(psObject.isEmpty()) {
        				break;
        			}
        			PriorityStrategy strategy = new JobGroup.PriorityStrategy();
        			strategy.setId(psid++);
        			strategy.setPriority(psObject.getInt("priority"));
        			strategy.setPriorityStrategyKey(psObject.getString("strategy"));
        			jobGroup.priorityStrategies.add(strategy);
        		}
        		if(jobGroup.priorityStrategies.isEmpty()) {
        			jobGroup.setUsePriorityStrategies(false);
        		}
            }
            return jobGroup;
        }
}
