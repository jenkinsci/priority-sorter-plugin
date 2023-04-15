package jenkins.advancedqueue.util;

import hudson.util.ListBoxModel;

public class PrioritySorterUtil {

    public static ListBoxModel fillPriorityItems(int to) {
        return fillPriorityItems(1, to);
    }

    public static ListBoxModel fillPriorityItems(int from, int to) {
        ListBoxModel items = new ListBoxModel();
        for (int i = from; i <= to; i++) {
            items.add(String.valueOf(i));
        }
        return items;
    }
}
