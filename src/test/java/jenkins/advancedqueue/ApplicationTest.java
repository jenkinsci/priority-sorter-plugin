package jenkins.advancedqueue;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.queue.QueueTaskFuture;
import jenkins.advancedqueue.sorter.ItemInfo;
import jenkins.advancedqueue.sorter.QueueItemCache;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class ApplicationTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Test
	public void simple_with_no_configuration() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		QueueTaskFuture<FreeStyleBuild> buildFuture = project.scheduleBuild2(1);
		ItemInfo item = QueueItemCache.get().getItem(buildFuture.get().getParent().getName());
		Assert.assertEquals(3, item.getPriority());
		buildFuture.get();
	}

	@Test
	@LocalData
	public void simple_with_basic_configuration() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		QueueTaskFuture<FreeStyleBuild> buildFuture = project.scheduleBuild2(1);
		ItemInfo item = QueueItemCache.get().getItem(buildFuture.get().getParent().getName());
		Assert.assertEquals(9, item.getPriority());
		buildFuture.get();
	}
}
