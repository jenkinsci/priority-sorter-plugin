/*
 * The MIT License
 *
 * Copyright 2013 Magnus Sandberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.advancedqueue;

public class PriorityCalculationsUtil {

	static private int PRIORITY_USE_DEFAULT_PRIORITY = -1;
	static private int PRIORITY_RUN_FIRST_PRIORITY = -2;
	static private int PRIORITY_RUN_LAST_PRIORITY = -3;

	static public int getUseDefaultPriorityPriority() {
		return PRIORITY_USE_DEFAULT_PRIORITY;
	}

	static public int getRunFirstPriority() {
		return PRIORITY_RUN_FIRST_PRIORITY;
	}

	static public int getRunLastPriority() {
		return PRIORITY_RUN_LAST_PRIORITY;
	}
	
	static public boolean isUseDefaultPriority(int priority) {
		return PRIORITY_USE_DEFAULT_PRIORITY == priority;
	}

	static public boolean isRunFirstPriority(int priority) {
		return PRIORITY_RUN_FIRST_PRIORITY == priority;
	}

	static public boolean isRunLastPriority(int priority) {
		return PRIORITY_RUN_LAST_PRIORITY == priority;
	}

	static public int scale(int oldmax, int newmax, int value) {
		if (value == PRIORITY_USE_DEFAULT_PRIORITY) {
			return PRIORITY_USE_DEFAULT_PRIORITY;
		}
		if (value == PRIORITY_RUN_FIRST_PRIORITY) {
			return PRIORITY_RUN_FIRST_PRIORITY;
		}
		if (value == PRIORITY_RUN_LAST_PRIORITY) {
			return PRIORITY_RUN_LAST_PRIORITY;
		}
		float p = ((float) (value - 1) / (float) (oldmax - 1));
		if (p <= 0.5) {
			return (int) (Math.floor(p * (newmax - 1))) + 1;
		}
		return (int) (Math.ceil(p * (newmax - 1))) + 1;
	}

}
