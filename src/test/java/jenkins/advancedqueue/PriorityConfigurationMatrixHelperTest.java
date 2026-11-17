package jenkins.advancedqueue;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import jenkins.advancedqueue.sorter.QueueItemCache;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PriorityConfigurationMatrixHelperTest {

    @Test
    void usesDefaultPriorityWhenMatrixProjectIsNotCached(JenkinsRule rule) {
        MatrixProject matrixProject = mock(MatrixProject.class);
        when(matrixProject.getName()).thenReturn("matrix-project-not-in-cache");
        MatrixConfiguration matrixConfiguration = mock(MatrixConfiguration.class);
        when(matrixConfiguration.getParent()).thenReturn(matrixProject);
        assertNull(QueueItemCache.get().getItem(matrixProject.getName()));

        int defaultPriority = PrioritySorterConfiguration.get().getStrategy().getDefaultPriority();
        PriorityConfigurationCallback callback = mock(PriorityConfigurationCallback.class);
        when(callback.setPrioritySelection(defaultPriority)).thenReturn(callback);

        PriorityConfigurationCallback result =
                new PriorityConfigurationMatrixHelper().getPriority(matrixConfiguration, callback);

        assertSame(callback, result);
        verify(callback).setPrioritySelection(defaultPriority);
    }
}
