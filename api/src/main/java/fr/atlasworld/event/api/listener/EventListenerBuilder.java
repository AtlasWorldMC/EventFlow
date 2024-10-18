package fr.atlasworld.event.api.listener;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import fr.atlasworld.common.annotation.OptionalBuilderArgument;
import fr.atlasworld.common.annotation.RequiredBuilderArgument;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.executor.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Event Listener Builder, simple interface to set custom options on your listener.
 *
 * @param <E> event type.
 */
public interface EventListenerBuilder<E extends Event> {

    /**
     * Sets the of this listener.
     *
     * @param executor executor that is executing the listener.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @RequiredBuilderArgument
    EventListenerBuilder<E> executor(@NotNull EventExecutor executor);

    /**
     * Sets the handler when the listener fails to properly handle the event.
     *
     * @param failureHandler consumer handing the exception.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @OptionalBuilderArgument
    EventListenerBuilder<E> failure(@NotNull Consumer<Throwable> failureHandler);

    /**
     * Sets the filter of this listener, if the condition do not match the event will not go through the listener.
     *
     * @param filter event filter.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @OptionalBuilderArgument
    EventListenerBuilder<E> filter(@NotNull Predicate<E> filter);

    /**
     * Sets how many times this listener will be executed before it expires.
     *
     * @param executions times the listener should run.
     *                   or {@code 0} if the listener doesn't expire.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @OptionalBuilderArgument
    EventListenerBuilder<E> expireCount(int executions);

    /**
     * Sets the expiration condition of this listener.
     *
     * @param condition predicate containing the condition.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @OptionalBuilderArgument
    EventListenerBuilder<E> expireWhen(@NotNull Predicate<E> condition);

    /**
     * Makes the listener expire when a specific event is received.
     *
     * @param eventType expiration event.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @OptionalBuilderArgument
    <T extends Event> EventListenerBuilder<E> expireWhen(@NotNull Class<T> eventType);

    /**
     * Sets the expiration condition of this listener.
     *
     * @param eventType expiration event.
     * @param condition predicate containing the condition.
     *
     * @return instance of this builder.
     */
    @NotNull
    @CanIgnoreReturnValue
    @OptionalBuilderArgument
    <T extends Event> EventListenerBuilder<E> expireWhen(@NotNull Class<T> eventType, @NotNull Consumer<T> condition);
}
