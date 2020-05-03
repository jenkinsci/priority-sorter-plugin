package jenkins.advancedqueue.util;

import hudson.util.ListBoxModel;

public class PrioritySorterUtil {
	
	static public ListBoxModel fillPriorityItems(int to) {
		return fillPriorityItems(1, to);
	}

	static public ListBoxModel fillPriorityItems(int from, int to) {
		ListBoxModel items = new ListBoxModel(to - from);
		for (int i = from; i <= to; i++) {
			items.add(String.valueOf(i));
		}
		return items;
	}

}
