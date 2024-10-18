package fr.atlasworld.event.core;

import com.google.common.base.Preconditions;
import fr.atlasworld.common.concurrent.action.CompositeFutureAction;
import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.event.api.listener.EventHandler;
import fr.atlasworld.event.api.listener.EventListener;
import fr.atlasworld.event.api.listener.EventListenerBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNodeImpl<E extends Event> implements EventNode<E> {
    private final String name;
    private final Class<E> eventType;
    private final Predicate<E> eventCondition;
    private final Map<String, EventNode<?>> childrens;
    private final Set<EventListener> listeners;

    public EventNodeImpl(String name, Class<E> eventType, Predicate<E> eventCondition) {
        this.name = name;
        this.eventType = eventType;

        this.eventCondition = eventCondition == null ? event -> true : eventCondition;

        this.childrens = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArraySet<>();
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull <T extends E> FutureAction<T> callEvent(@NotNull T event) {
        Preconditions.checkNotNull(event);

        CompositeFutureAction.CompositeBuilder futureBuilder = CompositeFutureAction.builder();


    }

    @Override
    public <T extends E> void addChildNode(@NotNull EventNode<T> node) {
        Preconditions.checkNotNull(node);

        this.childrens.put(node.name(), node);
    }

    @Override
    public EventNode<E> createChildNode(@NotNull String name) {
        Preconditions.checkNotNull(name);

        EventNode<E> node = new EventNodeImpl<>(name, this.eventType, null);

        this.childrens.put(name, node);
        return node;
    }

    @Override
    public EventNode<E> createChildNode(@NotNull String name, @NotNull Predicate<E> filter) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(filter);

        EventNode<E> node = new EventNodeImpl<>(name, this.eventType, filter);

        this.childrens.put(name, node);
        return node;
    }

    @Override
    public <T extends E> EventNode<T> createChildNode(@NotNull String name, @NotNull Class<T> eventType) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(eventType);

        EventNode<T> node = new EventNodeImpl<>(name, eventType, null);

        this.childrens.put(name, node);
        return node;
    }

    @Override
    public <T extends E> EventNode<T> createChildNode(@NotNull String name, @NotNull Class<T> eventType, @NotNull Predicate<T> filter) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(eventType);
        Preconditions.checkNotNull(filter);

        EventNode<T> node = new EventNodeImpl<>(name, eventType, filter);

        this.childrens.put(name, node);
        return node;
    }

    @Override
    public @Nullable EventNode<?> removeChildNode(@NotNull String name) {
        Preconditions.checkNotNull(name);

        return this.childrens.remove(name);
    }

    @Override
    public @Nullable EventNode<?> removeChildNode(@NotNull EventNode<?> node) {
        Preconditions.checkNotNull(node);

        return this.childrens.remove(node.name());
    }

    @Override
    public @NotNull Set<EventNode<?>> children() {
        return Set.copyOf(this.childrens.values());
    }

    @Override
    public @NotNull Optional<EventNode<?>> child(@NotNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(this.childrens.get(name));
    }

    @Override
    public <T extends E> void addListener(@NotNull Class<T> event, @NotNull EventHandler<T> handler, @NotNull Consumer<EventListenerBuilder<T>> builder) {

    }

    @Override
    public void addListener(@NotNull EventListener listener, @NotNull Consumer<EventListenerBuilder<E>> builder) {

    }
}
