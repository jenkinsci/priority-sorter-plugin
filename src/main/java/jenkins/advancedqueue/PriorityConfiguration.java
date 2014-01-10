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
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import jenkins.advancedqueue.priority.PriorityStrategy;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
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

	transient private Map<Integer, JobGroup> id2jobGroup;
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
		id2jobGroup = new HashMap<Integer, JobGroup>();
		for (JobGroup jobGroup : jobGroups) {
			id2jobGroup.put(jobGroup.getId(), jobGroup);
			Collections.sort(jobGroup.getPriorityStrategies(), new Comparator<JobGroup.PriorityStrategyHolder>() {
				public int compare(JobGroup.PriorityStrategyHolder o1, JobGroup.PriorityStrategyHolder o2) {
					return o1.getId() - o2.getId();
				}
			});
		}
	}

	public String getIconFileName() {
		if (!checkActive()) {
			return null;
		}
		return "/plugin/PrioritySorter/advqueue.png";
	}

	@Override
	public String getDisplayName() {
		return Messages.PriorityConfiguration_displayName();
	}

	public String getUrlName() {
		if (!checkActive()) {
			return null;
		}
		return "advanced-build-queue";
	}

	private boolean checkActive() {
		PrioritySorterConfiguration configuration = PrioritySorterConfiguration.get();
		if (configuration.getLegacyMode()) {
			return false;
		}
		if (configuration.getOnlyAdminsMayEditPriorityConfiguration()) {
			return Jenkins.getInstance().getACL().hasPermission(Jenkins.ADMINISTER);
		}
		return true;
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

	public JobGroup getJobGroup(int id) {
		return id2jobGroup.get(id);
	}

	public ExtensionList<Descriptor<PriorityStrategy>> getPriorityStrategyDescriptors() {
		return PriorityStrategy.all();
	}

	public ListBoxModel getPriorities() {
		ListBoxModel items = PrioritySorterConfiguration.get().doGetPriorityItems();
		return items;
	}

	public void doPriorityConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		jobGroups = new LinkedList<JobGroup>();
		id2jobGroup = new HashMap<Integer, JobGroup>();
		//
		String parameter = req.getParameter("json");
		JSONObject jobGroupsObject = JSONObject.fromObject(parameter);
		JSONArray jsonArray = JSONArray.fromObject(jobGroupsObject.get("jobGroup"));
		int id = 0;
		for (Object object : jsonArray) {
			JSONObject jobGroupObject = JSONObject.fromObject(object);
			if (jobGroupObject.isEmpty()) {
				break;
			}
			JobGroup jobGroup = JobGroup.newInstance(req, jobGroupObject, id++);
			jobGroups.add(jobGroup);
			id2jobGroup.put(jobGroup.getId(), jobGroup);
		}
		save();
		rsp.sendRedirect(Jenkins.getInstance().getRootUrl());
	}

	public Descriptor<PriorityConfiguration> getDescriptor() {
		return this;
	}

	public FormValidation doCheckJobPattern(@QueryParameter String value) throws IOException, ServletException {
		if (value.length() > 0) {
			try {
				Pattern.compile(value);
			} catch (PatternSyntaxException e) {
				return FormValidation.warning("The expression is not valid, please enter a valid expression.");
			}
		}
		return FormValidation.ok();
	}

	public PriorityConfigurationCallback getPriority(Queue.Item item, PriorityConfigurationCallback priorityCallback) {
		SecurityContext saveCtx = ACL.impersonate(ACL.SYSTEM);
		try {
			return getPriorityInternal(item, priorityCallback);
		} finally {
			SecurityContextHolder.setContext(saveCtx);
		}
	}

	private PriorityConfigurationCallback getPriorityInternal(Queue.Item item, PriorityConfigurationCallback priorityCallback) {

		if (!(item.task instanceof Job)) {
			// Not a job generally this mean that this is a lightweight task so
			// priority doesn't really matter - returning default priority
			priorityCallback.addDecisionLog(0, "Queue.Item is not a Job - Assigning global default priority");
			return priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority());
		}

		Job<?, ?> job = (Job<?, ?>) item.task;

		// [JENKINS-8597]
		// For MatrixConfiguration use the latest assigned Priority from the
		// MatrixProject
		if (job instanceof MatrixConfiguration) {
			MatrixProject matrixProject = ((MatrixConfiguration) job).getParent();
			priorityCallback.addDecisionLog(0, "Job is MatrixConfiguration [" + matrixProject.getName() + "] ...");
			ItemInfo itemInfo = QueueItemCache.get().getItem(matrixProject.getName());
			// Can be null (for example) at startup when the MatrixBuild got
			// lost (was running at
			// restart)
			if (itemInfo != null) {
				priorityCallback.addDecisionLog(0, "MatrixProject found in cache, using priority from queue-item [" + itemInfo.getItemId() + "]");
				return priorityCallback.setPrioritySelection(itemInfo.getPriority(), itemInfo.getJobGroupId(), itemInfo.getPriorityStrategy());
			}
			priorityCallback.addDecisionLog(0, "MatrixProject not found in cache, assigning global default priority");
			return priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority());
		}

		if (PrioritySorterConfiguration.get().getAllowPriorityOnJobs()) {
			AdvancedQueueSorterJobProperty priorityProperty = ((Job<?, ?>) job).getProperty(AdvancedQueueSorterJobProperty.class);
			if (priorityProperty != null && priorityProperty.getUseJobPriority()) {
				int priority = priorityProperty.priority;
				if (priority == PriorityCalculationsUtil.getUseDefaultPriorityPriority()) {
					priority = PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
				}
				priorityCallback.addDecisionLog(0, "Using priority taken directly from the Job");
				return priorityCallback.setPrioritySelection(priority);
			}
		}
		//
		JobGroup jobGroup = getJobGroup(priorityCallback, job.getName());
		if (jobGroup != null) {
			return getPriorityForJobGroup(priorityCallback, jobGroup, item);
		}
		//
		priorityCallback.addDecisionLog(0, "Assigning global default priority");
		return priorityCallback.setPrioritySelection(PrioritySorterConfiguration.get().getStrategy().getDefaultPriority());
	}

	// TODO a simple jobName will not work for jobs in folders, and would be
	// meaningless for non-Job Queue.Task’s
	public JobGroup getJobGroup(PriorityConfigurationCallback priorityCallback, String jobName) {
		for (JobGroup jobGroup : jobGroups) {
			priorityCallback.addDecisionLog(0, "Evaluating JobGroup [" + jobGroup.getId() + "] ...");
			Collection<View> views = Jenkins.getInstance().getViews();
			nextView: for (View view : views) {
				priorityCallback.addDecisionLog(1, "Evaluating View [" + view.getViewName() + "] ...");
				if (view.getViewName().equals(jobGroup.getView())) {
					// getItem() always returns the item
					TopLevelItem jobItem = view.getItem(jobName);
					// Now check if the item is actually in the view
					if (view.contains(jobItem)) {
						// If filtering is not used use the priority
						// If filtering is used but the pattern is empty regard
						// it as a match all
						if (!jobGroup.isUseJobFilter() || jobGroup.getJobPattern().trim().isEmpty()) {
							priorityCallback.addDecisionLog(2, "Not using filter ...");
							return jobGroup;
						} else {
							priorityCallback.addDecisionLog(2, "Using filter ...");
							// So filtering is on - use the priority if there's
							// a match
							try {
								if (jobName.matches(jobGroup.getJobPattern())) {
									priorityCallback.addDecisionLog(3, "Job is matching the filter ...");
									return jobGroup;
								} else {
									priorityCallback.addDecisionLog(3, "Job is not matching the filter ...");
									continue nextView;
								}
							} catch (PatternSyntaxException e) {
								// If the pattern is broken treat this a non
								// match
								priorityCallback.addDecisionLog(3, " Filter has syntax error");
								continue nextView;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private PriorityConfigurationCallback getPriorityForJobGroup(PriorityConfigurationCallback priorityCallback, JobGroup jobGroup, Queue.Item item) {
		int priority = jobGroup.getPriority();
		PriorityStrategy reason = null;
		if (jobGroup.isUsePriorityStrategies()) {
			priorityCallback.addDecisionLog(2, "Evaluating strategies ...");
			List<JobGroup.PriorityStrategyHolder> priorityStrategies = jobGroup.getPriorityStrategies();
			for (JobGroup.PriorityStrategyHolder priorityStrategy : priorityStrategies) {
				PriorityStrategy strategy = priorityStrategy.getPriorityStrategy();
				priorityCallback.addDecisionLog(3, "Evaluating strategy [" + strategy.getDescriptor().getDisplayName() + "] ...");
				if (strategy.isApplicable(item)) {
					priorityCallback.addDecisionLog(4, "Strategy is applicable");
					int foundPriority = strategy.getPriority(item);
					if (foundPriority > 0 && foundPriority <= PrioritySorterConfiguration.get().getStrategy().getNumberOfPriorities()) {
						priority = foundPriority;
						reason = strategy;
						break;
					}
				}
			}
		}
		if (reason == null) {
			priorityCallback.addDecisionLog(2, "No applicable strategy - Using JobGroup default");
		}
		if (priority == PriorityCalculationsUtil.getUseDefaultPriorityPriority()) {
			priority = PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
		}
		return priorityCallback.setPrioritySelection(priority, jobGroup.getId(), reason);
	}

	static public PriorityConfiguration get() {
		return (PriorityConfiguration) Jenkins.getInstance().getDescriptor(PriorityConfiguration.class);
	}

}
