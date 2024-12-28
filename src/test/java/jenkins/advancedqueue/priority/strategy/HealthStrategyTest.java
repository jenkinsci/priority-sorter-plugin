package jenkins.advancedqueue.priority.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.junit.Test;

public class HealthStrategyTest {
    private BuildableItem mockedBuildableItem;
    private Job<?, ?> mockedJob;

    @Test
    public void assertHealthOver80SameSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_OVER_80");
        setMockedJobHealthTo(85);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealthOver80SameSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_OVER_80");
        setMockedJobHealthTo(75);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealthOver80BetterSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_OVER_80");
        setMockedJobHealthTo(85);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealthOver80BetterSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_OVER_80");
        setMockedJobHealthTo(75);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80SameSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_61_TO_80");
        setMockedJobHealthTo(75);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80SameSelectionFailsWithGreater() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_61_TO_80");
        setMockedJobHealthTo(81);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80SameSelectionFailsWithLower() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_61_TO_80");
        setMockedJobHealthTo(60);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80BetterSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_61_TO_80");
        setMockedJobHealthTo(75);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80BetterSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_61_TO_80");
        setMockedJobHealthTo(60);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80WorseSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_61_TO_80");
        setMockedJobHealthTo(80);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth61To80WorseSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_61_TO_80");
        setMockedJobHealthTo(81);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60SameSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_41_TO_60");
        setMockedJobHealthTo(55);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60SameSelectionFailsWithGreater() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_41_TO_60");
        setMockedJobHealthTo(61);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60SameSelectionFailsWithLower() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_41_TO_60");
        setMockedJobHealthTo(40);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60BetterSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_41_TO_60");
        setMockedJobHealthTo(50);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60BetterSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_41_TO_60");
        setMockedJobHealthTo(40);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60WorseSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_41_TO_60");
        setMockedJobHealthTo(60);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth41to60WorseSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_41_TO_60");
        setMockedJobHealthTo(61);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40SameSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_21_TO_40");
        setMockedJobHealthTo(35);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40SameSelectionFailsWithGreater() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_21_TO_40");
        setMockedJobHealthTo(41);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40SameSelectionFailsWithLower() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_21_TO_40");
        setMockedJobHealthTo(20);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40BetterSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_21_TO_40");
        setMockedJobHealthTo(30);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40BetterSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_21_TO_40");
        setMockedJobHealthTo(20);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40WorseSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_21_TO_40");
        setMockedJobHealthTo(40);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth21to40WorseSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_21_TO_40");
        setMockedJobHealthTo(41);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20SameSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_0_TO_20");
        setMockedJobHealthTo(15);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20SameSelectionFailsWithGreater() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_0_TO_20");
        setMockedJobHealthTo(21);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20SameSelectionFailsWithLower() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "SAME", "HEALTH_0_TO_20");
        setMockedJobHealthTo(-1);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20BetterSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_0_TO_20");
        setMockedJobHealthTo(15);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20BetterSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "BETTER", "HEALTH_0_TO_20");
        setMockedJobHealthTo(-1);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20WorseSelectionConcludes() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_0_TO_20");
        setMockedJobHealthTo(20);
        initializeJobRunList();
        assertTrue(strategy.isApplicable(this.mockedBuildableItem));
    }

    @Test
    public void assertHealth0to20WorseSelectionFails() throws NoSuchFieldException, IllegalAccessException {
        HealthStrategy strategy = new HealthStrategy(0, "WORSE", "HEALTH_0_TO_20");
        setMockedJobHealthTo(21);
        initializeJobRunList();
        assertFalse(strategy.isApplicable(this.mockedBuildableItem));
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
