package jenkins.advancedqueue.test;

import jenkins.advancedqueue.PrioritySorterConfiguration;
import jenkins.advancedqueue.sorter.SorterStrategy;
import jenkins.advancedqueue.sorter.SorterStrategyDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import hudson.util.ListBoxModel;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class PrioritySorterConfigurationTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();
    private PrioritySorterConfiguration prioritySorterConfiguration;
    private SorterStrategyDescriptor sorterStrategyDescriptor1, sorterStrategyDescriptor2;

    @Before
    public void setUp() {
        prioritySorterConfiguration = new PrioritySorterConfiguration();
        sorterStrategyDescriptor1 = mock(SorterStrategyDescriptor.class);
        sorterStrategyDescriptor2 = mock(SorterStrategyDescriptor.class);
    }

    @Test
    public void testDoFillStrategyItems() {
        when(sorterStrategyDescriptor1.getDisplayName()).thenReturn("DisplayName1");
        when(sorterStrategyDescriptor1.getKey()).thenReturn("Key1");
        when(sorterStrategyDescriptor2.getDisplayName()).thenReturn("DisplayName2");
        when(sorterStrategyDescriptor2.getKey()).thenReturn("Key2");

        List<SorterStrategyDescriptor> mockStrategies = Arrays.asList(sorterStrategyDescriptor1, sorterStrategyDescriptor2);

        try (MockedStatic<SorterStrategy> mocked = mockStatic(SorterStrategy.class)) {
            mocked.when(SorterStrategy::getAllSorterStrategies).thenReturn(mockStrategies);

            ListBoxModel result = prioritySorterConfiguration.doFillStrategyItems();

            assertEquals(2, result.size());
            assertEquals("DisplayName1", result.get(0).name);
            assertEquals("Key1", result.get(0).value);
            assertEquals("DisplayName2", result.get(1).name);
            assertEquals("Key2", result.get(1).value);
        }
    }
}
