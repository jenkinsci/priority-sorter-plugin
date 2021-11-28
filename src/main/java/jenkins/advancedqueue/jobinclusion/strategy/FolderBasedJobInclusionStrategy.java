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
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jenkins.advancedqueue.DecisionLogger;
import jenkins.advancedqueue.Messages;
import jenkins.advancedqueue.jobinclusion.JobInclusionStrategy;
import jenkins.model.Jenkins;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.hudson.plugins.folder.Folder;

import javax.annotation.CheckForNull;

/**
 * @author Magnus Sandberg
 * @since 3.0
 */
public class FolderBasedJobInclusionStrategy extends JobInclusionStrategy {

	private final static Logger LOGGER = Logger.getLogger(FolderBasedJobInclusionStrategy.class.getName());

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

	@Restricted(NoExternalUse.class)
	static public class JobPattern {
		private String jobPattern;

		@DataBoundConstructor
		public JobPattern(String jobPattern) {
			this.jobPattern = jobPattern;
		}

	}

	private String folderName;

	private boolean useJobFilter = false;

	private String jobPattern;
	private transient Pattern compiledPattern;

	@DataBoundConstructor
	public FolderBasedJobInclusionStrategy(String folderName, JobPattern jobFilter) {
		this.folderName = folderName;
		this.useJobFilter = (jobFilter != null);
		if (this.useJobFilter) {
			this.jobPattern = jobFilter.jobPattern;
		}
	}

	public FolderBasedJobInclusionStrategy(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

	public boolean isUseJobFilter() {
		return useJobFilter;
	}

	@CheckForNull
	public String getJobPattern() {
		return jobPattern;
	}

	@CheckForNull
	private Pattern getCompiledPattern() throws PatternSyntaxException {
		if (jobPattern == null)
			return null;

		if (compiledPattern == null)
			compiledPattern = Pattern.compile(jobPattern);

		return compiledPattern;
	}

	@Override
	public boolean contains(DecisionLogger decisionLogger, Job<?, ?> job) {
		if (job.getFullName().startsWith(folderName)) {
			if (!isUseJobFilter() || getJobPattern() == null || getCompiledPattern() == null) {
				decisionLogger.addDecisionLog(2, "Not using filter ...");
				return true;
			} else {
				decisionLogger.addDecisionLog(2, "Using filter ...");
				try {
					if (getCompiledPattern().matcher(job.getName()).matches()) {
						decisionLogger.addDecisionLog(3, "Job is matching the filter ...");
						return true;
					} else {
						decisionLogger.addDecisionLog(3, "Job is not matching the filter ...");
						return false;
					}
				} catch (PatternSyntaxException e) {
					decisionLogger.addDecisionLog(3, "Filter has syntax error");
					return false;
				}
			}
		}
		return false;
	}
}
