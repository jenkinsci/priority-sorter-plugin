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
import static hudson.init.InitMilestone.PLUGINS_STARTED;
import hudson.Plugin;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.widgets.Widget;

import java.util.List;
import java.util.logging.Logger;

import jenkins.advancedqueue.priority.strategy.PriorityJobProperty;
import jenkins.advancedqueue.sorter.AdvancedQueueSorter;
import jenkins.advancedqueue.widgets.BuildQueueWidget;
import jenkins.model.Jenkins;

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

	@Initializer(before=PLUGINS_STARTED) 
	public static void addAliases() { 
		// Moved in 3.0 when JobPropertyStrategy was added
	   Items.XSTREAM2.addCompatibilityAlias("jenkins.advancedqueue.AdvancedQueueSorterJobProperty", PriorityJobProperty.class);
	}
	
	@Initializer(after = JOB_LOADED)
	public static void init() {
		// Check for any Legacy Configuration and init the Configuration
		LOGGER.info("Configuring the Priority Sorter ...");
		PrioritySorterConfiguration.init();
		// Init the Queue and sort the loaded Queue items
		LOGGER.info("Sorting existing Queue ...");
		AdvancedQueueSorter.init();
	}

}
