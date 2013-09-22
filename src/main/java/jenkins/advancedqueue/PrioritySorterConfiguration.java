package jenkins.advancedqueue;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AbstractProject;
import hudson.queueSorter.PrioritySorterJobProperty;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PrioritySorterConfiguration extends GlobalConfiguration {

	static int PRIORITY_USE_DEFAULT_PRIORITY = -1; 
	
	private boolean legacyMode = false;
	private Integer legacyMaxPriority = Integer.MAX_VALUE;
	private Integer legacyMinPriority = Integer.MIN_VALUE;

	private boolean allowPriorityOnJobs;
	private int numberOfPriorities;
	private int defaultPriority;
	private SorterStrategy strategy;
	
	public PrioritySorterConfiguration() {
		numberOfPriorities = 5;
		defaultPriority = 3;
		strategy = SorterStrategy.ABSOLUTE;
		allowPriorityOnJobs = true;
		checkLegacy();
		if(!getLegacyMode()) {
			load();
		}
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json)
			throws FormException {
		System.out.println(json);	
		int prevNumberOfPriorities = numberOfPriorities;
		numberOfPriorities = json.getInt("numberOfPriorities");
		defaultPriority = json.getInt("defaultPriority");
		allowPriorityOnJobs = json.getBoolean("allowPriorityOnJobs");
		strategy = SorterStrategy.valueOf(json.getString("strategy"));
		if(getLegacyMode()) {			
			Boolean advanced = json.getBoolean("advanced");
			if(advanced) {
				convertFromLegacyToAdvanced();
			}
		} else {
			updatePriorities(prevNumberOfPriorities);
		}
		save();
		return true;
	}

	public boolean getLegacyMode() {
		return legacyMode;
	}

	public int getNumberOfPriorities() {
		return numberOfPriorities;
	}

	public int getDefaultPriority() {
		return defaultPriority;
	}

	public boolean getAllowPriorityOnJobs() {
		return allowPriorityOnJobs;
	}

	public SorterStrategy getStrategy() {
		return strategy;
	}

	public int getUseDefaultPriorityPriority() {
		return PRIORITY_USE_DEFAULT_PRIORITY;
	}

	public ListBoxModel doFillDefaultPriorityItems() {
		return internalFillDefaultPriorityItems(getNumberOfPriorities());
	}

	public ListBoxModel doFillStrategyItems() {
		ListBoxModel strategies = new ListBoxModel();
		SorterStrategy[] values = SorterStrategy.values();
		for (SorterStrategy sorterStrategy : values) {
			strategies.add(sorterStrategy.getDisplayValue(), sorterStrategy.name());			
		}
		return strategies;
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

	public ListBoxModel doUpdateDefaultPriorityItems(@QueryParameter("value") String strValue) {
		int value = getNumberOfPriorities();
		try {
			value = Integer.valueOf(strValue);
		} catch(NumberFormatException e) {
			// Use default value
		}
		ListBoxModel items = internalFillDefaultPriorityItems(value);
		return items;
	}

	protected ListBoxModel doGetPriorityItems() {
		ListBoxModel items = internalFillDefaultPriorityItems(getNumberOfPriorities());
		items.add(0, new ListBoxModel.Option("-- use default priority --",
				String.valueOf(getUseDefaultPriorityPriority())));
		return items;
	}

	public FormValidation doCheckNumberOfPriorities(@QueryParameter String value)
			throws IOException, ServletException {
		if (value.length() == 0) {
			return FormValidation.error("Please enter a value.");
		}
		try {
			int intValue = Integer.parseInt(value);
			if (intValue <= 0) {
				return FormValidation
						.error("Please enter a positive numeric value.");
			}
		} catch (NumberFormatException e) {
			return FormValidation
					.error("Please enter a positive numeric value.");
		}
		return FormValidation.ok();
	}
	
	private void checkLegacy() {
		legacyMode = false;
		legacyMaxPriority = Integer.MAX_VALUE;
		legacyMinPriority = Integer.MIN_VALUE;
		
		@SuppressWarnings("rawtypes")
		List<AbstractProject> allProjects = Jenkins.getInstance().getAllItems(AbstractProject.class);
		for (AbstractProject<?, ?> project : allProjects) {
			PrioritySorterJobProperty priority = project.getProperty(PrioritySorterJobProperty.class);
			if (priority != null) {
				legacyMode = true;
				Math.min(legacyMinPriority, priority.priority);
				Math.max(legacyMaxPriority, priority.priority);
			}
		}
	}

	private void updatePriorities(int prevNumberOfPriorities) {
		@SuppressWarnings("rawtypes")
		List<AbstractProject> allProjects = Jenkins.getInstance().getAllItems(AbstractProject.class);
		for (AbstractProject<?, ?> project : allProjects) {
			AdvancedQueueSorterJobProperty priorityProperty = project.getProperty(AdvancedQueueSorterJobProperty.class);
			if(priorityProperty != null) {
				int newPriority = scale(prevNumberOfPriorities, getNumberOfPriorities(), priorityProperty.priority); 
				try {
					project.removeProperty(priorityProperty);
					project.addProperty(new AdvancedQueueSorterJobProperty(priorityProperty.getUseJobPriority(), newPriority));
				} catch (IOException e) {
					System.out.println("Failed to update Advanced Job Priority To " + project.getName());				
				}
			}
		}
		//
		List<JobGroup> jobGroups = PriorityConfiguration.get().getJobGroups();
		for (JobGroup jobGroup : jobGroups) {
			jobGroup.priority = scale(prevNumberOfPriorities, getNumberOfPriorities(), jobGroup.priority);
		}
		PriorityConfiguration.get().save();
	}
	
	private void convertFromLegacyToAdvanced() {
		// Update legacy range first
		checkLegacy();
		if(getLegacyMode()) {
			//
			@SuppressWarnings("rawtypes")
			List<AbstractProject> allProjects = Jenkins.getInstance().getAllItems(AbstractProject.class);
			for (AbstractProject<?, ?> project : allProjects) {
				PrioritySorterJobProperty legacyPriorityProperty = project.getProperty(PrioritySorterJobProperty.class);
				if (legacyPriorityProperty != null && getAllowPriorityOnJobs()) {
					int offset = normalizedOffset(legacyMinPriority, legacyMaxPriority);
					int normalized = inverseAndNormalize(legacyMinPriority, legacyMaxPriority, legacyPriorityProperty.priority);
					int advancedPriority = scale(legacyMaxPriority + offset, getNumberOfPriorities(), legacyPriorityProperty.priority);					
					AdvancedQueueSorterJobProperty advancedQueueSorterJobProperty = new AdvancedQueueSorterJobProperty(true, advancedPriority);
					try {
						project.addProperty(advancedQueueSorterJobProperty);
					} catch (IOException e) {
						System.out.println("Failed to add Advanced Job Priority To " + project.getName());
					}
				}
				try {
					project.removeProperty(legacyPriorityProperty);
				} catch (IOException e) {
					System.out.println("Failed to remove Legacy Job Priority From " + project.getName());
				}
			}
		}
	}

	static int normalizedOffset(int min, int max) {
		// Normalise from 1-<max>
		int offset = 0;
		if(min == 0) {
			offset = 1;
		}
		if(min < 1) {
			offset = -min + 1;
		}
		if(min > 1) {
			offset = min;
		}
		return offset;
	}

	static int inverseAndNormalize(int min, int max, int value) {
		int offset = normalizedOffset(min, max);
		min += offset;
		max += offset;
		value += offset;
		// Inverse
		value = max - value + 1;
		return value;
	}
	
	static int scale(int oldmax, int newmax, int value) {
		if(value == PRIORITY_USE_DEFAULT_PRIORITY) {
			return PRIORITY_USE_DEFAULT_PRIORITY;
		}
		float p = ((float) (value - 1) / (float) (oldmax - 1));
		if(p <= 0.5) {
			return (int) (Math.floor(p * (float) (newmax - 1))) + 1;
		}
		return (int) (Math.ceil(p * (float) (newmax - 1))) + 1;
	}


	static public PrioritySorterConfiguration get() {
		ExtensionList<GlobalConfiguration> extensionList = GlobalConfiguration.all();
		for (GlobalConfiguration globalConfiguration : extensionList) {
			if (globalConfiguration instanceof PrioritySorterConfiguration) {
				return (PrioritySorterConfiguration) globalConfiguration;
			}
		}
		throw new RuntimeException();
	}

}
