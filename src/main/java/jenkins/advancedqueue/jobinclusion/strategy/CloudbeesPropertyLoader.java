package jenkins.advancedqueue.jobinclusion.strategy;

import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.util.DescribableList;
import jenkins.advancedqueue.DecisionLogger;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;

public class CloudbeesPropertyLoader {

	static public String getJobViewName(DecisionLogger decisionLogger, Job<?, ?> job) {
		ItemGroup<?> parent = job.getParent();
		decisionLogger.addDecisionLog(2, "Checking for Cloudbees Folder inclusion ...");
		while(parent != null) {
			if(parent instanceof Folder) {
				Folder folder = (Folder) parent;
				decisionLogger.addDecisionLog(3, "Evaluating Folder [" + folder.getFullName() + "] ...");
				DescribableList<FolderProperty<?>,FolderPropertyDescriptor> properties = folder.getProperties();
				for(FolderProperty<?> property : properties) {
					if(property instanceof JobInclusionCloudbeesFolderProperty) {
						JobInclusionCloudbeesFolderProperty incProperty = (JobInclusionCloudbeesFolderProperty) property;
						if(incProperty.isUseJobGroup()) {
							String name = incProperty.getJobGroupName();
							decisionLogger.addDecisionLog(4, "JobGroup is enabled, with JobGroup [" + name + "] ...");
							return name;
						}
					}
				}
			}
			if(parent instanceof TopLevelItem) {
				parent = ((TopLevelItem) parent).getParent();				
			} else {
				parent = null;
			}
		}
		decisionLogger.addDecisionLog(2, "No match ...");
		return null;
	}

}
