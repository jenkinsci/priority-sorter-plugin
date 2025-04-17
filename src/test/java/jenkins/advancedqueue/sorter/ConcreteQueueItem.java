package jenkins.advancedqueue.sorter;

import hudson.model.Queue;

public interface ConcreteQueueItem {
    void enter(Queue q);

    boolean leave(Queue q);
}
