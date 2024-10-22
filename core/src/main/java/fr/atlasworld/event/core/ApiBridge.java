package fr.atlasworld.event.core;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ApiBridge implements fr.atlasworld.event.api.internal.ApiBridge {
    @Override
    public <E extends Event> EventNode<E> createEventNode(String name, Class<E> eventType, @Nullable Predicate<E> filter) {
        return new EventNodeImpl<>(name, eventType, filter);
    }
}
