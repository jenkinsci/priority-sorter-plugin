package hudson.queueSorter;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.views.ListViewColumn;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * PrioritySorterJobColumn
 * 
 * Column plugin that adds a column to a jobs overview page.
 * 
 * The column displays priority set for the job.
 *
 *
 *
 * @author c3johnso
 */
public class PrioritySorterJobColumn extends ListViewColumn{

    public String getPriority(Job job)
    {
        PrioritySorterJobProperty jp = (PrioritySorterJobProperty) job.getProperty(PrioritySorterJobProperty.class);
        if (jp != null) {
            return Integer.toString(jp.priority);
        } else {
            // No priority has been set for this job - use empty
            return "100";
        }
    }

    @Extension
    public static final Descriptor<ListViewColumn> DESCRIPTOR = new DescriptorImpl();
    
    @Override
    public Descriptor<ListViewColumn> getDescriptor() {
        return DESCRIPTOR;
    }

    private static class DescriptorImpl extends Descriptor<ListViewColumn> {
        @Override
        public ListViewColumn newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new PrioritySorterJobColumn();
        }
        @Override
        public String getDisplayName() {
            return "Priority Value";
        }
    }
}
