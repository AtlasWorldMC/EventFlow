package fr.atlasworld.event.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EventContext {
    private final Set<EventNodeImpl<?>> calledNodes;

    public EventContext() {
        this.calledNodes = ConcurrentHashMap.newKeySet();
    }

    public void registerCalled(EventNodeImpl<?> node) {
        this.calledNodes.add(node);
    }

    public boolean wasCalled(EventNodeImpl<?> node) {
        return this.calledNodes.contains(node);
    }
}
