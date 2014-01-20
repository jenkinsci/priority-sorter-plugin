package jenkins.advancedqueue.jobinclusion.strategy;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class JobInclusionJobProperty extends JobProperty<AbstractProject<?, ?>> {

	private boolean useJobGroup;

	private String jobGroupName;

	@DataBoundConstructor
	public JobInclusionJobProperty(Boolean useJobGroup, String jobGroupName) {
		this.useJobGroup = useJobGroup;
		this.jobGroupName = jobGroupName;
	}

	public String getJobGroupName() {
		return jobGroupName;
	}
	
	public boolean isUseJobGroup() {
		return useJobGroup;
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {
		
		@Override
		public String getDisplayName() {
			return "XXX";
		}

		public ListBoxModel getJobGroups() {
			return PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
		}

		public boolean isUsed() {
			return PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups().size() > 0;
		}
	}

}
