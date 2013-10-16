package jenkins.advancedqueue;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Describable;
import hudson.model.JobProperty;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.View;
import hudson.security.Permission;
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

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class PriorityConfiguration extends Descriptor<PriorityConfiguration> implements RootAction, Describable<PriorityConfiguration> {
	
	private final static Logger LOGGER = Logger.getLogger(PriorityConfiguration.class.getName());
			
	private List<JobGroup> jobGroups;
	
	public PriorityConfiguration() {
		super(PriorityConfiguration.class);
		jobGroups = new LinkedList<JobGroup>();
		load();
		Collections.sort(jobGroups, new Comparator<JobGroup>() {
			public int compare(JobGroup o1, JobGroup o2) {
				return o1.id - o2.id;
			}
		});
	}

	public String getIconFileName() {
		if(PrioritySorterConfiguration.get().getLegacyMode()) {
			return null;
		}
		return "/plugin/PrioritySorter/advqueue.png";
	}

	public String getDisplayName() {
		return "Job Priorities";
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
			if(jobGroupObject.size() == 0) {
				break;
			}
			JobGroup jobGroup = new JobGroup();
			jobGroup.id = id++;
			jobGroup.priority = jobGroupObject.getInt("priority");
			jobGroup.view = jobGroupObject.getString("view");
			jobGroup.useJobFilter = jobGroupObject.has("useJobFilter");
			if(jobGroup.useJobFilter) {
				JSONObject jsonObject = jobGroupObject.getJSONObject("useJobFilter");
				jobGroup.jobPattern = jsonObject.getString("jobPattern");
				// Disable the filter if the pattern is invalid
				try {
					Pattern.compile(jobGroup.jobPattern);
				} catch (PatternSyntaxException e) {
					jobGroup.useJobFilter = false;		
				}
			}
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

	public int getPriority(Job<?,?> job) {
		// Get priority
		int priority = getPriorityValue(job);
		try {
			// And cache the calculated value on the Job
			ActualAdvancedQueueSorterJobProperty jp = job.getProperty(ActualAdvancedQueueSorterJobProperty.class);
			if(jp == null) {
				jp = new ActualAdvancedQueueSorterJobProperty(priority);
				((AbstractProject<?,?>) job).addProperty(jp);
			} else {
				jp.priority = priority;			
			}
			job.save();
		} catch (Exception e) {
			LOGGER.warning("Failed to add Actual Advanced Job Priority To " + job.getName());
		}
		return priority;
	}
	
	private int getPriorityValue(Job<?,?> job) {
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
				if(view.getViewName().equals(jobGroup.view)) {
					TopLevelItem jobItem = view.getItem(job.getName());
					if(jobItem != null) {
						int priority = PrioritySorterConfiguration.get().getUseDefaultPriorityPriority();
						// If filtering is not used use the priority
						// If filtering is used but the pattern is empty regard it as a match all
						if(!jobGroup.useJobFilter || jobGroup.jobPattern.trim().isEmpty()) {
							priority = jobGroup.priority;
						} else {
							// So filtering is on - use the priority if there's a match
							try {
								if(job.getName().matches(jobGroup.jobPattern)) {
									priority = jobGroup.priority;								
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
	
	static public PriorityConfiguration get() {
		return (PriorityConfiguration) Jenkins.getInstance().getDescriptor(PriorityConfiguration.class);
	}

	
}
