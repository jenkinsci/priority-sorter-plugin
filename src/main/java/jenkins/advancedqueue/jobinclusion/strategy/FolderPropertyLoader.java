/*
 * The MIT License
 *
 * Copyright (c) 2014, Magnus Sandberg
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
package jenkins.advancedqueue.jobinclusion.strategy;

import javax.annotation.CheckForNull;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;

import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.util.DescribableList;
import jenkins.advancedqueue.DecisionLogger;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
public class FolderPropertyLoader {

	@CheckForNull    
	static public String getJobGroupName(DecisionLogger decisionLogger, Job<?, ?> job) {
		ItemGroup<?> parent = job.getParent();
		decisionLogger.addDecisionLog(2, "Checking for Cloudbees Folder inclusion ...");
		while(parent != null) {
			if(parent instanceof AbstractFolder) {
				AbstractFolder<?> folder = (AbstractFolder<?>) parent;
				decisionLogger.addDecisionLog(3, "Evaluating Folder [" + folder.getFullName() + "] ...");
				DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> properties = folder.getProperties();
				for(AbstractFolderProperty<?> property : properties) {
					if(property instanceof JobInclusionFolderProperty) {
						JobInclusionFolderProperty incProperty = (JobInclusionFolderProperty) property;
						if(incProperty.isUseJobGroup()) {
							String name = incProperty.getJobGroupName();
							decisionLogger.addDecisionLog(4, "JobGroup is enabled, with JobGroup [" + name + "] ...");
							return name;
						}
					}
				}
			}
			if(parent instanceof TopLevelItem) {
				parent = ((TopLevelItem) parent).getParent();				
			} else {
				parent = null;
			}
		}
		decisionLogger.addDecisionLog(2, "No match ...");
		return null;
	}

}
