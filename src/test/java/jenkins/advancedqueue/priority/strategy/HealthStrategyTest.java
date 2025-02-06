package jenkins.advancedqueue.priority.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import hudson.model.HealthReport;
import hudson.model.Job;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Task;
import hudson.util.RunList;
import java.lang.reflect.Field;
import java.util.Iterator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class HealthStrategyTest {
    private BuildableItem mockedBuildableItem;
    private Job<?, ?> mockedJob;

    static Object[][] data() {
        return new Object[][] {
            {"SAME", "HEALTH_OVER_80", 85, true},
            {"SAME", "HEALTH_OVER_80", 75, false},
            {"BETTER", "HEALTH_OVER_80", 85, true},
            {"BETTER", "HEALTH_OVER_80", 75, false},
            {"SAME", "HEALTH_61_TO_80", 75, true},
            {"SAME", "HEALTH_61_TO_80", 81, false},
            {"SAME", "HEALTH_61_TO_80", 60, false},
            {"BETTER", "HEALTH_61_TO_80", 75, true},
            {"BETTER", "HEALTH_61_TO_80", 60, false},
            {"WORSE", "HEALTH_61_TO_80", 80, true},
            {"WORSE", "HEALTH_61_TO_80", 81, false},
            {"SAME", "HEALTH_41_TO_60", 55, true},
            {"SAME", "HEALTH_41_TO_60", 61, false},
            {"SAME", "HEALTH_41_TO_60", 40, false},
            {"BETTER", "HEALTH_41_TO_60", 50, true},
            {"BETTER", "HEALTH_41_TO_60", 40, false},
            {"WORSE", "HEALTH_41_TO_60", 60, true},
            {"WORSE", "HEALTH_41_TO_60", 61, false},
            {"SAME", "HEALTH_21_TO_40", 35, true},
            {"SAME", "HEALTH_21_TO_40", 41, false},
            {"SAME", "HEALTH_21_TO_40", 20, false},
            {"BETTER", "HEALTH_21_TO_40", 30, true},
            {"BETTER", "HEALTH_21_TO_40", 20, false},
            {"WORSE", "HEALTH_21_TO_40", 40, true},
            {"WORSE", "HEALTH_21_TO_40", 41, false},
            {"SAME", "HEALTH_0_TO_20", 15, true},
            {"SAME", "HEALTH_0_TO_20", 21, false},
            {"SAME", "HEALTH_0_TO_20", -1, false},
            {"BETTER", "HEALTH_0_TO_20", 15, true},
            {"BETTER", "HEALTH_0_TO_20", -1, false},
            {"WORSE", "HEALTH_0_TO_20", 20, true},
            {"WORSE", "HEALTH_0_TO_20", 21, false}
        };
    }

    @ParameterizedTest(name = "{0} - {1} - actual: {2} - concludes: {3}")
    @MethodSource("data")
    void assertHealth(String selection, String health, int mockedHealth, boolean expected) throws Exception {
        HealthStrategy strategy = new HealthStrategy(0, selection, health);
        setMockedJobHealthTo(mockedHealth);
        initializeJobRunList();

        if (expected) {
            assertTrue(strategy.isApplicable(this.mockedBuildableItem));
        } else {
            assertFalse(strategy.isApplicable(this.mockedBuildableItem));
        }
    }

    private void setMockedJobHealthTo(int health) throws NoSuchFieldException, IllegalAccessException {
        this.mockedBuildableItem = mock(BuildableItem.class);
        this.mockedJob = mock(Job.class, withSettings().extraInterfaces(Task.class));
        setTaskInMockedBuildableItem(this.mockedBuildableItem, (Task) this.mockedJob);
        HealthReport mockedHealthReport = mock(HealthReport.class);
        when(this.mockedJob.getBuildHealth()).thenReturn(mockedHealthReport);
        when(mockedHealthReport.getScore()).thenReturn(health);
    }

    private void initializeJobRunList() {
        // These mocks are used to avoid returning false instantly as per HealthStrategy#isApplicable:68
        when(this.mockedJob.getBuilds()).thenReturn(mock(RunList.class));
        when(this.mockedJob.getBuilds().iterator()).thenReturn(mock(Iterator.class));
        when(this.mockedJob.getBuilds().iterator().hasNext()).thenReturn(true);
    }

    private void setTaskInMockedBuildableItem(BuildableItem buildableItem, Task task)
            throws NoSuchFieldException, IllegalAccessException {
        Field taskField = BuildableItem.class.getField("task");
        taskField.setAccessible(true);
        taskField.set(buildableItem, task);
    }
}
