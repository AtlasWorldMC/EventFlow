package fr.atlasworld.event.core;

import com.google.common.base.Preconditions;
import fr.atlasworld.common.concurrent.action.CompositeFutureAction;
import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.common.concurrent.action.SimpleFutureAction;
import fr.atlasworld.common.logging.LogUtils;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import fr.atlasworld.event.api.listener.EventHandler;
import fr.atlasworld.event.api.listener.EventListener;
import fr.atlasworld.event.api.listener.EventListenerBuilder;
import fr.atlasworld.event.core.listener.LambdaRegisteredListener;
import fr.atlasworld.event.core.listener.ListenerSettings;
import fr.atlasworld.event.core.listener.MethodRegisteredListener;
import fr.atlasworld.event.core.listener.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ThreadSafe
public class EventNodeImpl<E extends Event> implements EventNode<E> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String name;
    private final Class<E> eventType;
    private final Predicate<E> eventCondition;
    private final Map<String, EventNodeImpl<?>> children;
    private final Map<Class<? extends E>, List<RegisteredListener<E>>> listeners;

    private final AtomicInteger parents;

    public EventNodeImpl(String name, Class<E> eventType, Predicate<E> eventCondition) {
        this.name = name;
        this.eventType = eventType;

        this.eventCondition = eventCondition == null ? event -> true : eventCondition;

        this.children = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentHashMap<>();
        this.parents = new AtomicInteger(0);
    }

    public void addParent() {
        this.parents.incrementAndGet();
    }

    public void removeParent() {
        this.parents.decrementAndGet();
    }

    public boolean hasParents() {
        return this.parents.get() > 0;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull <T extends E> FutureAction<T> callEvent(@NotNull T event) {
        Preconditions.checkNotNull(event);

        if (this.hasParents())
            throw new UnsupportedOperationException("Current node is not the root of the tree! " +
                    "Events must get called on the root node.");

        return this.invokeEvent(event);
    }

    private <T extends E> FutureAction<T> invokeEvent(@NotNull T event) {
        if (!this.eventCondition.test(event))
            return new SimpleFutureAction<T>().complete(event);

        CompositeFutureAction.CompositeBuilder builder = CompositeFutureAction.builder();

        for (EventNodeImpl<?> node : this.children.values()) {
            builder.add(node.propagateEvent(event));
        }

        if (this.listeners.containsKey(event.getClass())) {
            for (RegisteredListener<E> listener : this.listeners.get(event.getClass())) {
                if (listener.isExpired(event)) {
                    // Clear expired listeners, less computing required for next call and loses reference for GC.
                    this.listeners.get(event.getClass()).remove(listener);
                    continue;
                }

                builder.add(listener.callEvent(event));
            }
        }

        // Bad Design, TODO (AtlasCommon): Allow Composite Future actions to be empty.
        builder.add(new SimpleFutureAction<>().complete(null)); // Prevents IllegalArgument if the event was executed nowhere.

        SimpleFutureAction<T> future = new SimpleFutureAction<>();
        FutureAction<Void> groupedFuture = builder.build();
        groupedFuture.onFailure(future::fail)
                .onSuccess(unused -> future.complete(event));

        return future;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Event> FutureAction<T> propagateEvent(@NotNull T event) {
        Preconditions.checkNotNull(event);

        if (!this.eventType.isInstance(event)) // Check if the event is the same as this event type.
            return new SimpleFutureAction<T>().complete(event);

        return (FutureAction<T>) this.invokeEvent((E) event);
    }

    @Override
    public <T extends E> void addChildNode(@NotNull EventNode<T> node) {
        Preconditions.checkNotNull(node);
        Preconditions.checkArgument(node instanceof EventNodeImpl, "Unsupported EventNode.");

        EventNodeImpl<T> nodeImpl = (EventNodeImpl<T>) node;
        nodeImpl.addParent();

        this.children.put(node.name(), nodeImpl);
    }

    @Override
    public EventNode<E> createChildNode(@NotNull String name) {
        Preconditions.checkNotNull(name);

        EventNodeImpl<E> node = new EventNodeImpl<>(name, this.eventType, null);
        node.addParent();

        this.children.put(name, node);
        return node;
    }

    @Override
    public EventNode<E> createChildNode(@NotNull String name, @NotNull Predicate<E> filter) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(filter);

        EventNodeImpl<E> node = new EventNodeImpl<>(name, this.eventType, filter);
        node.addParent();

        this.children.put(name, node);
        return node;
    }

    @Override
    public <T extends E> EventNode<T> createChildNode(@NotNull String name, @NotNull Class<T> eventType) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(eventType);

        EventNodeImpl<T> node = new EventNodeImpl<>(name, eventType, null);
        node.addParent();

        this.children.put(name, node);
        return node;
    }

    @Override
    public <T extends E> EventNode<T> createChildNode(@NotNull String name, @NotNull Class<T> eventType, @NotNull Predicate<T> filter) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(eventType);
        Preconditions.checkNotNull(filter);

        EventNodeImpl<T> node = new EventNodeImpl<>(name, eventType, filter);
        node.addParent();

        this.children.put(name, node);
        return node;
    }

    @Override
    public @Nullable EventNode<?> removeChildNode(@NotNull String name) {
        Preconditions.checkNotNull(name);

        EventNodeImpl<?> node = this.children.remove(name);
        if (node != null)
            node.removeParent();

        return node;
    }

    @Override
    public @Nullable EventNode<?> removeChildNode(@NotNull EventNode<?> node) {
        Preconditions.checkNotNull(node);

        return this.removeChildNode(node.name());
    }

    @Override
    public @NotNull Set<EventNode<?>> children() {
        return Set.copyOf(this.children.values());
    }

    @Override
    public @NotNull Optional<EventNode<?>> child(@NotNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(this.children.get(name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> void addListener(@NotNull Class<T> eventType, @NotNull EventHandler<T> handler, @NotNull Consumer<EventListenerBuilder<T>> builder) {
        Preconditions.checkNotNull(eventType);
        Preconditions.checkNotNull(handler);
        Preconditions.checkNotNull(builder);

        ListenerSettings.Builder<T> settings = new ListenerSettings.Builder<>();
        builder.accept(settings);

        synchronized (this.listeners) {
            this.listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add((LambdaRegisteredListener<E>) new LambdaRegisteredListener<T>(settings.build(), handler));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addListener(@NotNull EventListener listener, @NotNull Consumer<EventListenerBuilder<E>> builder) {
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(builder);

        ListenerSettings.Builder<E> settings = new ListenerSettings.Builder<>();
        builder.accept(settings);
        Class<? extends EventListener> listenerClass = listener.getClass();

        for (Method method : listenerClass.getDeclaredMethods()) {

            if (!method.isAnnotationPresent(fr.atlasworld.event.api.annotation.EventHandler.class)) {
                LOGGER.debug("Method {}#{} skipped, not annotated with @EventHandler.", listenerClass.getSimpleName(), method.getName());
                continue;
            }

            if (method.getParameterCount() != 1) {
                LOGGER.error("Method {}#{} has more than one parameter!", listenerClass.getSimpleName(), method.getName());
                continue;
            }

            Parameter parameter = method.getParameters()[0];

            if (!Event.class.isAssignableFrom(parameter.getType())) {
                LOGGER.error("Method {}#{} parameter is not an event!", listenerClass.getSimpleName(), method.getName());
                continue;
            }

            if (!this.eventType.isAssignableFrom(parameter.getType())) {
                LOGGER.debug("WARN: Method {}#{} parameter event will never get called, event doesn't inherit this node event type.",
                        listenerClass.getSimpleName(), method.getName());

                // Even if it is a valid listener method,
                // it's a waste to register a listener that will never get called.
                continue;
            }

            Class<? extends E> eventClass = (Class<? extends E>) parameter.getType();
            MethodRegisteredListener<E> methodListener =
                    new MethodRegisteredListener<>(settings.build(), listener, method);

            this.listeners.computeIfAbsent(eventClass, k -> new ArrayList<>())
                    .add(methodListener);
        }
    }
}
