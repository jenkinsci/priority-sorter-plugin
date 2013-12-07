/*
 * The MIT License
 *
 * Copyright 2013 Magnus Sandberg, Oleg Nenashev
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

import static hudson.init.InitMilestone.JOB_LOADED;
import hudson.Plugin;
import hudson.init.Initializer;

import java.util.logging.Logger;

import jenkins.advancedqueue.sorter.AdvancedQueueSorter;

/**
 * Plugin is the staring point of the Priority Sorter Plugin.
 * 
 * Used to make sure that the data is initialized at startup.
 * 
 * @author Magnus Sandberg
 * @since 2.3
 */
public class PrioritySorterPlugin extends Plugin {

	private final static Logger LOGGER = Logger.getLogger(PrioritySorterPlugin.class.getName());

	@Initializer(after = JOB_LOADED)
	public static void init() {
		// Check for Legacy Mode and init the Configuration
		LOGGER.info("Configuring the Priority Sorter ...");
		PrioritySorterConfiguration.init();
		// If Legacy Mode - init the Queue and sort the loaded Queue items
		if (PrioritySorterConfiguration.get().getLegacyMode()) {
			LOGGER.info("Sorting existing Queue ...");
			AdvancedQueueSorter.init();
		}
	}

}
