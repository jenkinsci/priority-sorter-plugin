/*
 * The MIT License
 *
 * Copyright (c) 2013, Magnus Sandberg
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import jenkins.advancedqueue.sorter.ItemInfo;

/**
 * @author Magnus Sandberg
 * @since 2.4
 */
public class ItemTransitionLogger {

	private final static Logger LOGGER = Logger.getLogger("PrioritySorter.Queue.Items");

	static public void logNewItem(@Nonnull ItemInfo info) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("New Item: " + info.toString() + "\n" + info.getDescisionLog());
		} else {
			LOGGER.fine("New Item: " + info.toString());
		}
	}

	static public void logBlockedItem(@Nonnull ItemInfo info) {
		LOGGER.fine("Blocking: " + info.toString());
	}

	static public void logBuilableItem(@Nonnull ItemInfo info) {
		LOGGER.fine("Buildable: " + info.toString());
	}

	static public void logStartedItem(@Nonnull ItemInfo info) {
		LOGGER.fine("Starting: " + info.toString());
	}

	static public void logCanceledItem(@Nonnull ItemInfo info) {
		LOGGER.fine("Canceling: " + info.toString());
	}

}
