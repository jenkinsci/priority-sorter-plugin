package jenkins.advancedqueue.testutil;


public class ExpectedItem {

	private String jobName;
	private int priority;

	public ExpectedItem(String jobName, int priority) {
		this.jobName = jobName;
		this.priority = priority;
	}

	public String getJobName() {
		return jobName;
	}

	public int getPriority() {
		return priority;
	}

}
