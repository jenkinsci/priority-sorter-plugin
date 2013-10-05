package jenkins.advancedqueue;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Describable;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.View;
import hudson.security.Permission;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class PriorityConfiguration extends Descriptor<PriorityConfiguration> implements RootAction, Describable<PriorityConfiguration> {
	
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
			}
			jobGroups.add(jobGroup);
		}
		save();
		rsp.sendRedirect(Jenkins.getInstance().getRootUrl());
	}

	public Descriptor<PriorityConfiguration> getDescriptor() {
		return this;
	}
	
	public int getPriority(Job<?,?> job) {
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
			for (View view : views) {
				if(view.getViewName().equals(jobGroup.view)) {
					TopLevelItem jobItem = view.getItem(job.getName());
					if(jobItem != null) {
						if(jobGroup.jobPattern.trim().isEmpty() || job.getName().matches(jobGroup.jobPattern)) {
							int priority = jobGroup.priority;
							if(priority == PrioritySorterConfiguration.get().getUseDefaultPriorityPriority()) {
								priority = PrioritySorterConfiguration.get().getDefaultPriority();
							}
							return priority;
						}
					}
				}
			}
		}
		//
		return PrioritySorterConfiguration.get().getDefaultPriority();
	}
	
	static public PriorityConfiguration get() {
		ExtensionList<RootAction> extensionList = Jenkins.getInstance().getExtensionList(RootAction.class);
		for (RootAction rootAction : extensionList) {
			if (rootAction instanceof PriorityConfiguration) {
				return (PriorityConfiguration) rootAction;
			}
		}
		throw new RuntimeException();
	}

	
}
