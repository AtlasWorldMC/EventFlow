package fr.atlasworld.event.api;

import fr.atlasworld.common.reflection.ReflectionFactory;
import fr.atlasworld.event.api.internal.ApiBridge;
import org.jetbrains.annotations.ApiStatus;

public final class EventFlow {
    @ApiStatus.Internal
    public static final ApiBridge BRIDGE = ReflectionFactory.loadSingleService(ApiBridge.class);

    private EventFlow() {
        throw new UnsupportedOperationException();
    }
}
