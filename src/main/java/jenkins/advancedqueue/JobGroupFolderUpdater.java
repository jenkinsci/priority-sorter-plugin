package jenkins.advancedqueue;

import com.cloudbees.hudson.plugins.folder.Folder;
import java.io.IOException;
import java.util.Map;
import jenkins.advancedqueue.jobinclusion.strategy.JobInclusionFolderProperty;
import jenkins.model.Jenkins;

/** Updates folder references only when the optional Folders plugin is installed. */
final class JobGroupFolderUpdater {
    private JobGroupFolderUpdater() {}

    static void update(Map<String, String> renamedGroups) throws IOException {
        for (Folder folder : Jenkins.get().getAllItems(Folder.class)) {
            JobInclusionFolderProperty property = folder.getProperties().get(JobInclusionFolderProperty.class);
            if (property != null && property.isUseJobGroup()) {
                String replacement = renamedGroups.get(property.getJobGroupName());
                if (replacement != null) {
                    property.setJobGroupName(replacement);
                    folder.save();
                }
            }
        }
    }
}
