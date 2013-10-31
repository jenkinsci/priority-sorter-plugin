package jenkins.advancedqueue;

public class PriorityCalculationsUtil {

	static private int PRIORITY_USE_DEFAULT_PRIORITY = -1;

	static public int getUseDefaultPriorityPriority() {
		return PRIORITY_USE_DEFAULT_PRIORITY;
	}

	static public int scale(int oldmax, int newmax, int value) {
		if (value == PRIORITY_USE_DEFAULT_PRIORITY) {
			return PRIORITY_USE_DEFAULT_PRIORITY;
		}
		float p = ((float) (value - 1) / (float) (oldmax - 1));
		if (p <= 0.5) {
			return (int) (Math.floor(p * (newmax - 1))) + 1;
		}
		return (int) (Math.ceil(p * (newmax - 1))) + 1;
	}

}
