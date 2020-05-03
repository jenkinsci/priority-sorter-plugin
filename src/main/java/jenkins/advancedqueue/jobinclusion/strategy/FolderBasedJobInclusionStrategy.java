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

import hudson.Extension;
import hudson.model.Job;
import hudson.util.ListBoxModel;

import java.util.List;

import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.Messages;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.hudson.plugins.folder.Folder;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
@Extension
public class FolderBasedJobInclusionStrategy extends JobInclusionStrategy {

	@Extension(optional = true)
	static public class FolderBasedJobInclusionStrategyDescriptor extends
			AbstractJobInclusionStrategyDescriptor<FolderBasedJobInclusionStrategy> {

		public FolderBasedJobInclusionStrategyDescriptor() {
			super(Messages.Jobs_included_in_folder());
		}

		public ListBoxModel getListFolderItems() {
			ListBoxModel items = new ListBoxModel();
			List<Folder> folders = Jenkins.get().getAllItems(Folder.class);
			for (Folder folder : folders) {
				items.add(folder.getFullName(), folder.getFullName());
			}
			return items;
		}

	};

	private String folderName;

	public FolderBasedJobInclusionStrategy() {
	}

	@DataBoundConstructor
	public FolderBasedJobInclusionStrategy(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

	@Override
	public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job) {
		return job.getFullName().startsWith(folderName);
	}
}
