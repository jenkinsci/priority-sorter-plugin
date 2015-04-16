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
import hudson.util.ListBoxModel;

import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
public class JobInclusionFolderProperty extends FolderProperty<Folder> {

	private boolean useJobGroup;

	private String jobGroupName;

	@DataBoundConstructor
	public JobInclusionFolderProperty(Boolean useJobGroup, String jobGroupName) {
		this.useJobGroup = useJobGroup;
		this.jobGroupName = jobGroupName;
	}

	public String getJobGroupName() {
		return jobGroupName;
	}
	
	public boolean isUseJobGroup() {
		return useJobGroup;
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends FolderPropertyDescriptor {
		
		@Override
		public String getDisplayName() {
			return "XXX";
		}

		public ListBoxModel getJobGroups() {
			return PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups();
		}

		public boolean isUsed() {
			return PropertyBasedJobInclusionStrategy.getPropertyBasesJobGroups().size() > 0;
		}
	}

}
