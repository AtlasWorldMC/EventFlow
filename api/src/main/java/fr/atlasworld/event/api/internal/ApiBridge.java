package fr.atlasworld.event.api.internal;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Internal Stuff, messing with this is highly not recommended.
 */
@ApiStatus.Internal
public interface ApiBridge {

    public <E extends Event> EventNode<E> createEventNode(String name, Class<E> eventType, @Nullable Predicate<E> filter);
}
