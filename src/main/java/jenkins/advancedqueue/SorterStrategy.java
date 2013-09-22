package jenkins.advancedqueue;

public enum SorterStrategy {

	FIFO("First In First Out"),
	ABSOLUTE("Absolute"),
	FQ("Fair Queueing"),
	WFQ("Weighted Fair Queueing");

	private final String displayValue;
	
	SorterStrategy(String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}
	
}
