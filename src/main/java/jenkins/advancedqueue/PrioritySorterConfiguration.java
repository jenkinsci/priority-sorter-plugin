package jenkins.advancedqueue;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AbstractProject;
import hudson.queueSorter.PrioritySorterJobProperty;
import hudson.slaves.OfflineCause;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PrioritySorterConfiguration extends GlobalConfiguration {

	private final static Logger LOGGER = Logger.getLogger(PrioritySorterConfiguration.class.getName());
	
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
		int prevNumberOfPriorities = numberOfPriorities;
		//
		numberOfPriorities = json.getInt("numberOfPriorities");
		FormValidation numberOfPrioritiesCheck = doCheckNumberOfPriorities(String.valueOf(numberOfPriorities));
		if(numberOfPrioritiesCheck.kind != FormValidation.Kind.OK) {
			throw new FormException(numberOfPrioritiesCheck.getMessage(), "numberOfPriorities");
		}
		//
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

	public final boolean getLegacyMode() {
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

	public FormValidation doCheckNumberOfPriorities(@QueryParameter String value) {
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
				legacyMaxPriority = Math.max(legacyMaxPriority, priority.priority);
				legacyMinPriority = Math.min(legacyMinPriority, priority.priority);
			}
		}
	}

	private void updatePriorities(int prevNumberOfPriorities) {
		@SuppressWarnings("rawtypes")
		List<AbstractProject> allProjects = Jenkins.getInstance().getAllItems(AbstractProject.class);
		for (AbstractProject<?, ?> project : allProjects) {
			try {
				// Remove the calculated priority 
				project.removeProperty(ActualAdvancedQueueSorterJobProperty.class);
				// Scale any priority on the Job
				AdvancedQueueSorterJobProperty priorityProperty = project.getProperty(AdvancedQueueSorterJobProperty.class);
				if(priorityProperty != null) {
					int newPriority = scale(prevNumberOfPriorities, getNumberOfPriorities(), priorityProperty.priority); 
						project.removeProperty(priorityProperty);
						project.addProperty(new AdvancedQueueSorterJobProperty(priorityProperty.getUseJobPriority(), newPriority));
						project.save();
				}
				project.save();
			} catch (IOException e) {
				LOGGER.warning("Failed to update Advanced Job Priority To " + project.getName());				
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
					int advancedPriority = legacyPriorityToAdvancedPriority(legacyMinPriority, legacyMaxPriority, getNumberOfPriorities(), legacyPriorityProperty.priority);
					AdvancedQueueSorterJobProperty advancedQueueSorterJobProperty = new AdvancedQueueSorterJobProperty(true, advancedPriority);
					try {
						project.addProperty(advancedQueueSorterJobProperty);
						project.save();
					} catch (IOException e) {
						LOGGER.warning("Failed to add Advanced Job Priority To " + project.getName());
					}
				}
				try {
					project.removeProperty(legacyPriorityProperty);
					project.save();
				} catch (IOException e) {
					LOGGER.warning("Failed to remove Legacy Job Priority From " + project.getName());
				}
			}
		}
	}
	
	static int legacyPriorityToAdvancedPriority(int legacyMinPriority, int legacyMaxPriority, int numberOfPriorities, int priority) {
		int offset = normalizedOffset(legacyMinPriority);
		int normalized = inverseAndNormalize(legacyMinPriority, legacyMaxPriority, priority);
		int advancedPriority = scale(legacyMaxPriority + offset, numberOfPriorities, normalized);
		return advancedPriority;
	}

	/**
	 * Calculates how much must be added to a legacy value to get into the positive numbers
	 */
	static int normalizedOffset(int min) {
		int offset = -min + 1;
		return offset;
	}

	static int inverseAndNormalize(int min, int max, int value) {
		int offset = normalizedOffset(min);
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
		return (PrioritySorterConfiguration) Jenkins.getInstance().getDescriptor(PrioritySorterConfiguration.class);
	}

}
