package jenkins.advancedqueue.test;

import hudson.model.Cause;
import hudson.model.Run;
import java.util.Objects;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

public class BuildUpstreamCause extends Cause.UpstreamCause {
    private final String nodeId;

    public BuildUpstreamCause(FlowNode node, Run<?, ?> invokingRun) {
        super(invokingRun);
        this.nodeId = node.getId();
    }

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BuildUpstreamCause that = (BuildUpstreamCause) o;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nodeId);
    }
}
