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
package jenkins.advancedqueue;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Describable;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Queue$Item;
import hudson.model.Queue.Task;
import hudson.model.View;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author Magnus Sandberg
 * @since 2.0
 */
@Extension
public class PriorityConfiguration extends Descriptor<PriorityConfiguration> implements RootAction, Describable<PriorityConfiguration> {
	
	private final static Logger LOGGER = Logger.getLogger(PriorityConfiguration.class.getName());
			
	private List<JobGroup> jobGroups;
	
	public PriorityConfiguration() {
		super(PriorityConfiguration.class);
		jobGroups = new LinkedList<JobGroup>();
		load();
		//
		Collections.sort(jobGroups, new Comparator<JobGroup>() {
			public int compare(JobGroup o1, JobGroup o2) {
				return o1.getId() - o2.getId();
			}
		});
		//
		for (JobGroup jobGroup : jobGroups) {
			Collections.sort(jobGroup.getPriorityStrategies(), new Comparator<JobGroup.PriorityStrategy>() {
				public int compare(JobGroup.PriorityStrategy o1, JobGroup.PriorityStrategy o2) {
					return o1.getId() - o2.getId();
				}
			});
			
		}
	}

	public String getIconFileName() {
		if(PrioritySorterConfiguration.get().getLegacyMode()) {
			return null;
		}
		return "/plugin/PrioritySorter/advqueue.png";
	}

	public String getDisplayName() {
		return Messages.PriorityConfiguration_displayName();
	}

	public String getUrlName() {
		if(PrioritySorterConfiguration.get().getLegacyMode()) {
			return null;
		}
		return "advanced-build-queue";
	}

	public List<JobGroup> getJobGroups() {
		return jobGroups;
	}

	public ListBoxModel getListViewItems() {
		ListBoxModel items = new ListBoxModel();
		Collection<View> views = Jenkins.getInstance().getViews();
		for (View view : views) {
			items.add(view.getDisplayName(), view.getViewName());	
		}
		return items;
	}
	
	public ListBoxModel getPriorityStrategyItems() {
		ListBoxModel items = new ListBoxModel();
		ExtensionList<PriorityStrategy> all = PriorityStrategy.all();
		for (PriorityStrategy priorityStrategy : all) {
			items.add(priorityStrategy.getDisplayName(), priorityStrategy.getKey());	
		}
		return items;
	}
	

	public ListBoxModel getPriorities() {
		ListBoxModel items = PrioritySorterConfiguration.get().doGetPriorityItems();
		return items;
	}

	
	public void doPriorityConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		jobGroups = new LinkedList<JobGroup>();
		//
		String parameter = req.getParameter("json");
		JSONObject jobGroupsObject = JSONObject.fromObject(parameter);
		JSONArray jsonArray = JSONArray.fromObject(jobGroupsObject.get("jobGroup"));
		int id = 0;
		for (Object object : jsonArray) {
			JSONObject jobGroupObject = JSONObject.fromObject(object);
			if(jobGroupObject.isEmpty()) {
				break;
			}
			JobGroup jobGroup = JobGroup.Create(jobGroupObject, id++);
			jobGroups.add(jobGroup);
		}
		save();
		// Removed cached priority values
		@SuppressWarnings("rawtypes")
		List<AbstractProject> allProjects = Jenkins.getInstance().getAllItems(AbstractProject.class);
		for (AbstractProject<?, ?> project : allProjects) {
			try {
				// Remove the calculated priority 
				project.removeProperty(ActualAdvancedQueueSorterJobProperty.class);
				project.save();
			} catch (IOException e) {
				LOGGER.warning("Failed to update Actual Advanced Job Priority To " + project.getName());				
			}
		}
		//
		rsp.sendRedirect(Jenkins.getInstance().getRootUrl());
	}

	public Descriptor<PriorityConfiguration> getDescriptor() {
		return this;
	}
	
	public FormValidation doCheckJobPattern(@QueryParameter String value)
			throws IOException, ServletException {
		if (value.length() > 0) {
			try {
				Pattern.compile(value);
			} catch (PatternSyntaxException e) {
				return FormValidation.warning("The expression is not valid, please enter a valid expression.");				
			}
		}
		return FormValidation.ok();
	}

	public int getPriority(Queue$Item item) {
		// Get priority
		Job<?, ?> job = (Job<?, ?>) item.task;
		int priority = getPriorityValue(item);
		try {
			// And cache the calculated value on the Job
			ActualAdvancedQueueSorterJobProperty jp = job.getProperty(ActualAdvancedQueueSorterJobProperty.class);
			if(jp == null) {
				jp = new ActualAdvancedQueueSorterJobProperty(priority);
				((AbstractProject<?,?>) job).addProperty(jp);
			} else {
				jp.setPriority(priority);			
			}
			job.save();
		} catch (Exception e) {
			LOGGER.warning("Failed to add Actual Advanced Job Priority To " + job.getName());
		}
		return priority;
	}
	
	private int getPriorityValue(Queue$Item item) {
		Job<?, ?> job = (Job<?, ?>) item.task;
		
		if(PrioritySorterConfiguration.get().getAllowPriorityOnJobs()) {
			AdvancedQueueSorterJobProperty priorityProperty = job.getProperty(AdvancedQueueSorterJobProperty.class);
			if (priorityProperty != null && priorityProperty.getUseJobPriority()) {
				int priority = priorityProperty.priority;
				if(priority == PrioritySorterConfiguration.get().getUseDefaultPriorityPriority()) {
					priority = PrioritySorterConfiguration.get().getDefaultPriority();
				}
				return priority;
			} 
		}
		//
		for (JobGroup jobGroup : jobGroups) {
			Collection<View> views = Jenkins.getInstance().getViews();
			nextView: for (View view : views) {
				if(view.getViewName().equals(jobGroup.getView())) {
					TopLevelItem jobItem = view.getItem(job.getName());
					if(jobItem != null) {
						int priority = PrioritySorterConfiguration.get().getUseDefaultPriorityPriority();
						// If filtering is not used use the priority
						// If filtering is used but the pattern is empty regard it as a match all
						if(!jobGroup.isUseJobFilter() || jobGroup.getJobPattern().trim().isEmpty()) {
							priority = getPriorityForJobGroup(jobGroup, item);
						} else {
							// So filtering is on - use the priority if there's a match
							try {
								if(job.getName().matches(jobGroup.getJobPattern())) {
									priority = getPriorityForJobGroup(jobGroup, item);								
								} else {
									continue nextView;
								}
							} catch (PatternSyntaxException e) {
								// If the pattern is broken treat this a non match
								continue nextView;
							}
						}
						if(priority == PrioritySorterConfiguration.get().getUseDefaultPriorityPriority()) {
							priority = PrioritySorterConfiguration.get().getDefaultPriority();
						}
						return priority;
					}
				}
			}
		}
		//
		return PrioritySorterConfiguration.get().getDefaultPriority();
	}
	
	private int getPriorityForJobGroup(JobGroup jobGroup, Queue$Item item) {
		if(jobGroup.isUsePriorityStrategies()) {
			List<JobGroup.PriorityStrategy> priorityStrategies = jobGroup.getPriorityStrategies();
			for (JobGroup.PriorityStrategy priorityStrategy : priorityStrategies) {
				String priorityStrategyKey = priorityStrategy.getPriorityStrategyKey();
				PriorityStrategy strategy = PriorityStrategy.getStrategyFromKey(priorityStrategyKey);
				if(strategy.isApplicable(item)) {
					return priorityStrategy.getPriority();
				}
			}
		}
		return jobGroup.getPriority();
	}
	
	static public PriorityConfiguration get() {
		return (PriorityConfiguration) Jenkins.getInstance().getDescriptor(PriorityConfiguration.class);
	}

	
}
