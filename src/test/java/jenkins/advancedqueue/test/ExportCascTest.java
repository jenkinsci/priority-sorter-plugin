package jenkins.advancedqueue.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.casc.model.Scalar;
import io.jenkins.plugins.casc.model.Sequence;
import io.jenkins.plugins.casc.yaml.YamlSource;
import io.jenkins.plugins.casc.yaml.YamlUtils;
import java.util.Collections;
import jenkins.advancedqueue.PriorityConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class ExportCascTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    @LocalData
    public void exportTest() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        Mapping expectedConfig = YamlUtils.loadFrom(
                Collections.singletonList(YamlSource.of(ConfigurationAsCodeTest.class
                        .getResource("ExportCascTest/PriorityConfiguration.yaml")
                        .toString())),
                context);
        expectedConfig = expectedConfig
                .get("unclassified")
                .asMapping()
                .get("priorityConfiguration")
                .asMapping();

        PriorityConfiguration configuration =
                (PriorityConfiguration) r.jenkins.getDescriptor(PriorityConfiguration.class);
        Configurator<PriorityConfiguration> c = context.lookupOrFail(PriorityConfiguration.class);

        CNode node = c.describe(configuration, context);
        assertNotNull(node);
        Mapping exportedConfig = node.asMapping();

        assertEqualsMapping(expectedConfig, exportedConfig);
    }

    private static void assertEqualsMapping(Mapping expectedMapping, Mapping actualMapping)
            throws ConfiguratorException {
        for (String key : expectedMapping.keySet()) {
            CNode expected = expectedMapping.get(key);
            assertTrue(actualMapping.containsKey(key));
            CNode actual = actualMapping.get(key);
            assertEquals(expected.getClass(), actual.getClass());
            if (expected instanceof Mapping) {
                assertEqualsMapping(expected.asMapping(), actual.asMapping());
            } else if (expected instanceof Sequence) {
                assertEqualsSequence(expected.asSequence(), actual.asSequence());
            } else if (expected instanceof Scalar) {
                assertEquals(expected.asScalar().getValue(), actual.asScalar().getValue());
            }
        }
        assertEquals(expectedMapping.keySet().size(), actualMapping.keySet().size());
    }

    private static void assertEqualsSequence(Sequence expectedSequence, Sequence actualSequence)
            throws ConfiguratorException {
        assertEquals(expectedSequence.size(), actualSequence.size());
        for (int i = 0; i < expectedSequence.size(); i++) {
            CNode expected = expectedSequence.get(i);
            CNode actual = actualSequence.get(i);
            if (expected instanceof Mapping) {
                assertEqualsMapping(expected.asMapping(), actual.asMapping());
            } else if (expected instanceof Sequence) {
                assertEqualsSequence(expected.asSequence(), actual.asSequence());
            } else if (expected instanceof Scalar) {
                assertEquals(expected.asScalar().getValue(), actual.asScalar().getValue());
            }
        }
    }
}
