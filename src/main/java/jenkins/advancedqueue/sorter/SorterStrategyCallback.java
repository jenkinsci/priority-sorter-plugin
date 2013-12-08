package jenkins.advancedqueue.sorter;

public interface SorterStrategyCallback {

	int getPriority();
	
	SorterStrategyCallback setWeightSelection(float weight);
}
