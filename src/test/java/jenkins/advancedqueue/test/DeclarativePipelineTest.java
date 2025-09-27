package jenkins.advancedqueue.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Descriptor.FormException;
import hudson.model.Queue;
import java.io.IOException;
import jenkins.advancedqueue.jobinclusion.strategy.JobInclusionJobProperty;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

@WithJenkins
class DeclarativePipelineTest {

    private static final String DEFAULT_SCRIPT =
            """
                pipeline {
                    agent none
                    stages {
                        stage('Test') {
                            steps {
                                echo 'Hello World!'
                            }
                        }
                    }
                }
            """;
    private static final String PRIORITY_SCRIPT =
            """
                pipeline {
                    agent none
                    options {
                        jobGroup(jobGroupName: '%s', useJobGroup: true)
                    }
                    stages {
                        stage('Test') {
                            steps {
                                echo 'Hello World!'
                            }
                        }
                    }
                }
            """;

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule j) throws Exception {
        this.j = j;
    }

    @Test
    @LocalData
    void test_declarative_pipeline() throws Exception {
        WorkflowJob job1 = createPipelineJob("job1", PRIORITY_SCRIPT, "P1");
        WorkflowJob job2 = createPipelineJob("job2", PRIORITY_SCRIPT, "P2");
        WorkflowJob job3 = createPipelineJob("job3", PRIORITY_SCRIPT, "P3");
        WorkflowJob job4 = createPipelineJob("job4", PRIORITY_SCRIPT, "P4");
        WorkflowJob job5 = createPipelineJob("job5", PRIORITY_SCRIPT, "P5");
        Queue.withLock(() -> {
            j.jenkins.getQueue().schedule2(job1, 0);
            j.jenkins.getQueue().schedule2(job2, 0);
            j.jenkins.getQueue().schedule2(job3, 0);
            j.jenkins.getQueue().schedule2(job4, 0);
            j.jenkins.getQueue().schedule2(job5, 0);
            j.jenkins.getQueue().maintain();
        });
        j.waitUntilNoActivity();
        j.assertBuildStatusSuccess(job1.getLastBuild());
        j.assertBuildStatusSuccess(job2.getLastBuild());
        j.assertBuildStatusSuccess(job3.getLastBuild());
        j.assertBuildStatusSuccess(job4.getLastBuild());
        j.assertBuildStatusSuccess(job5.getLastBuild());
        JobInclusionJobProperty prop1 = job1.getProperty(JobInclusionJobProperty.class);
        assertEquals("P1", prop1.getJobGroupName());
        assertTrue(prop1.isUseJobGroup());
        JobInclusionJobProperty prop2 = job2.getProperty(JobInclusionJobProperty.class);
        assertEquals("P2", prop2.getJobGroupName());
        assertTrue(prop2.isUseJobGroup());
        JobInclusionJobProperty prop3 = job3.getProperty(JobInclusionJobProperty.class);
        assertEquals("P3", prop3.getJobGroupName());
        assertTrue(prop3.isUseJobGroup());
        JobInclusionJobProperty prop4 = job4.getProperty(JobInclusionJobProperty.class);
        assertEquals("P4", prop4.getJobGroupName());
        assertTrue(prop4.isUseJobGroup());
        JobInclusionJobProperty prop5 = job5.getProperty(JobInclusionJobProperty.class);
        assertEquals("P5", prop5.getJobGroupName());
        assertTrue(prop5.isUseJobGroup());
    }

    @Test
    @LocalData
    void test_declarative_pipeline_default_null() throws Exception {
        WorkflowJob job = createPipelineJob("job", DEFAULT_SCRIPT, null);
        job.scheduleBuild2(0);
        j.waitUntilNoActivity();
        j.assertBuildStatusSuccess(job.getLastBuild());
        JobInclusionJobProperty prop = job.getProperty(JobInclusionJobProperty.class);
        assertNull(prop);
    }

    private WorkflowJob createPipelineJob(String jobName, String script, String jobGroupName) throws FormException, IOException {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, jobName);
        job.setDefinition(new CpsFlowDefinition(String.format(script, jobGroupName), true));
        return job;
    }
}
