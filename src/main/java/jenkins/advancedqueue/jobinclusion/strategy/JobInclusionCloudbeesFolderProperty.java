package jenkins.advancedqueue.jobinclusion.strategy;

import hudson.Extension;
import hudson.model.Descriptor.FormException;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;

public class JobInclusionCloudbeesFolderProperty extends FolderProperty<Folder> {

	private boolean useJobGroup;

	private String jobGroupName;

	@DataBoundConstructor
	public JobInclusionCloudbeesFolderProperty(Boolean useJobGroup, String jobGroupName) {
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
	public static final class DescriptorImpl extends FolderPropertyDescriptor {
		
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
