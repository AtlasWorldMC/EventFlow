package fr.atlasworld.event.core;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.event.api.internal.ApiBridge;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ApiBridgeImpl implements ApiBridge {
    @Override
    public <E extends Event> EventNode<E> createEventNode(String name, Class<E> eventType, @Nullable Predicate<E> filter) {
        return new EventNodeImpl<>(name, eventType, filter);
    }
}
