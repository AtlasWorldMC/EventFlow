package fr.atlasworld.event.api;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import fr.atlasworld.event.api.listener.EventHandler;
import fr.atlasworld.event.api.listener.EventListener;
import fr.atlasworld.event.api.listener.EventListenerBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The event system is in a tree structure.
 * <p>
 * An EventNode represents a branch of that tree,
 * the tree always starts with a root node.
 * <br>
 * The EventNode can filter out events,
 * for example it could allow to filter player related events and only
 * accept event specific to a player, or it's rank.
 *
 * @param <E> event type.
 */
public interface EventNode<E extends Event> {

    /**
     * Create a new event node.
     *
     * @param name name of the node.
     *
     * @return newly created node.
     */
    static EventNode<Event> create(@NotNull String name) {
        Preconditions.checkNotNull(name);

        return EventFlow.BRIDGE.createEventNode(name, Event.class, null);
    }

    /**
     * Create a new event node.
     *
     * @param name name of the node.
     * @param filter predicate to test whether an event should be called on the node.
     *
     * @return newly created node.
     */
    static EventNode<Event> create(@NotNull String name, @NotNull Predicate<Event> filter) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(filter);

        return EventFlow.BRIDGE.createEventNode(name, Event.class, filter);
    }

    /**
     * Create a new event node.
     *
     * @param name name of the node.
     * @param eventType event type.
     *
     * @return newly created node.
     */
    static <E extends Event> EventNode<E> create(@NotNull String name, @NotNull Class<E> eventType) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(eventType);

        return EventFlow.BRIDGE.createEventNode(name, eventType, null);
    }

    /**
     * Create a new event node.
     *
     * @param name name of the node.
     * @param eventType event type.
     * @param filter predicate to test whether an event should be called on the node.
     *
     * @return newly created node.
     */
    static <E extends Event> EventNode<E> create(@NotNull String name, @NotNull Class<E> eventType, @NotNull Predicate<E> filter) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(eventType);
        Preconditions.checkNotNull(filter);

        return EventFlow.BRIDGE.createEventNode(name, eventType, filter);
    }

    /**
     * Retrieve the name of this node.
     *
     * @return name of this node.
     */
    @NotNull
    String name();

    /**
     * Call an event on this node.
     * <p>
     * This will also call the event on child nodes.
     *
     * @param event event.
     *
     * @return future, once completed containing the event after being passed to the listeners.
     * @throws NullPointerException if {@code event} is {@code null}.
     */
    @NotNull
    @CanIgnoreReturnValue
    <T extends E> CompletableFuture<T> callEvent(@NotNull T event);

    /**
     * Adds a child node to this node.
     *
     * @param node node to be added.
     *
     * @param <T> event type, extending this node type.
     *
     * @throws NullPointerException if {@code event} is {@code null}.
     */
    <T extends E> void addChildNode(@NotNull EventNode<T> node);

    /**
     * Create a new child node.
     *
     * @param name name of the node.
     *
     * @return newly created node.
     */
    EventNode<E> createChildNode(@NotNull String name);

    /**
     * Create a new child node.
     *
     * @param name name of the node.
     * @param filter filter of the node.
     *
     * @return newly created node.
     */
    EventNode<E> createChildNode(@NotNull String name, @NotNull Predicate<E> filter);

    /**
     * Create a new child node.
     *
     * @param name name of the node.
     * @param eventType event type of the node.
     *
     * @return newly created node.
     */
    <T extends E> EventNode<T> createChildNode(@NotNull String name, @NotNull Class<T> eventType);

    /**
     * Create a new child node.
     *
     * @param name name of the node.
     * @param eventType event type of the node.
     * @param filter predicate to test whether an event should be called on the node.
     *
     * @return newly created node.
     */
    <T extends E> EventNode<T> createChildNode(@NotNull String name, @NotNull Class<T> eventType, @NotNull Predicate<T> filter);

    /**
     * Remove a child node.
     *
     * @param name name of the node to remove.
     *
     * @return instance of the removed node, or {@code null} if no child node was found.
     */
    @Nullable
    @CanIgnoreReturnValue
    EventNode<?> removeChildNode(@NotNull String name);

    /**
     * Remove a child node.
     *
     * @param node node to be removed.
     *
     * @return instance of the removed node, or {@code null} if no child node was found.
     */
    @Nullable
    @CanIgnoreReturnValue
    EventNode<?> removeChildNode(@NotNull EventNode<?> node);

    /**
     * Retrieve all the child nodes.
     *
     * @return child {@link EventNode}.
     */
    @NotNull
    Set<EventNode<?>> children();

    /**
     * Retrieve a child node.
     *
     * @param name name of the child node to retrieve.
     *
     * @return optional containing the child node, or empty optional if the child could not be found.
     */
    @NotNull
    Optional<EventNode<?>> child(String name);


    /**
     * Adds a listener to this node.
     *
     * @param event event to listen for.
     * @param handler event handler.
     *
     * @param <T> event type.
     * @throws NullPointerException if {@code eventClass} or {@code event} is {@code null}.
     */
    default <T extends E> void addListener(@NotNull Class<T> event, @NotNull EventHandler<T> handler) {
        this.addListener(event, handler, builder -> {});
    }

    /**
     * Adds a listener to this node.
     *
     * @param event event to listen for.
     * @param handler event handler.
     * @param builder listener builder.
     *
     * @param <T> event type.
     * @throws NullPointerException if {@code eventClass}, {@code handler} or {@code builder} is {@code null}.
     */
    <T extends E> void addListener(@NotNull Class<T> event, @NotNull EventHandler<T> handler, @NotNull Consumer<EventListenerBuilder<T>> builder);

    /**
     * Adds a {@link EventListener} class to this node.
     *
     * @param listener listener class.
     *
     * @throws NullPointerException if {@code listener} is {@code null}.
     */
    default void addListener(@NotNull EventListener listener) {
        this.addListener(listener, builder -> {});
    }

    /**
     * Adds a {@link EventListener} class to this node.
     *
     * @param listener listener class.
     * @param builder listener builder.
     *
     * @throws NullPointerException if {@code listener} or {@code builder} is {@code null}.
     */
    void addListener(@NotNull EventListener listener, @NotNull Consumer<EventListenerBuilder<E>> builder);
}
