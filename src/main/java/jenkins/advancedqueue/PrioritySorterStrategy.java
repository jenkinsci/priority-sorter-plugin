package jenkins.advancedqueue;

import hudson.ExtensionList;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;

import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;

import org.apache.tools.ant.ExtensionPoint;

public abstract class PrioritySorterStrategy extends ExtensionPoint {

	public abstract SorterStrategy getSorterStrategy();
	
	/**
	 * Called when a new {@link hudson.model.Item} enters the queue.
	 * 
	 * @param item the {@link hudson.model.WaitingItem} that enters the queue
	 * @return the weight of the item in the queue, lower value will give sooner start
	 */
	public abstract float onNewItem(WaitingItem item);
	
	/**
	 * Called when a {@link hudson.model.Item} leaves the queue and it is started.
	 * 
	 * @param item the {@link hudson.model.LeftItem}
	 * @param weight the weight assigned when the item entered the queue
	 */
	public void onStartedItem(LeftItem item, float weight) {}

	/**
	 * Called when a {@link hudson.model.Item} leaves the queue and it is canceled.
	 */
	public void onCanceledItem(LeftItem item) {};

	public static List<SorterStrategy> getAllSorterStrategies() {
		ExtensionList<PrioritySorterStrategy> all = all();
		ArrayList<SorterStrategy> strategies = new ArrayList<SorterStrategy>(all.size());
		for (PrioritySorterStrategy prioritySorterStrategy : all) {
			strategies.add(prioritySorterStrategy.getSorterStrategy());
		}
		return strategies;
	}
	
	public static SorterStrategy getSorterStrategy(String key) {
		List<SorterStrategy> allSorterStrategies = getAllSorterStrategies();
		for (SorterStrategy sorterStrategy : allSorterStrategies) {
			if(key.equals(sorterStrategy.getKey())) {
				return sorterStrategy;
			}
		}
		return null;
	}

	public static PrioritySorterStrategy getPrioritySorterStrategy(SorterStrategy sorterStrategy) {
		ExtensionList<PrioritySorterStrategy> all = all();
		for (PrioritySorterStrategy prioritySorterStrategy : all) {
			if(prioritySorterStrategy.getSorterStrategy().getKey().equals(sorterStrategy.getKey())) {
				return prioritySorterStrategy;
			}
		}
		return null;
	}

	
	/**
     * All registered {@link PrioritySorterStrategy}s.
     */
    public static ExtensionList<PrioritySorterStrategy> all() {
        return Jenkins.getInstance().getExtensionList(PrioritySorterStrategy.class);
    }
}
