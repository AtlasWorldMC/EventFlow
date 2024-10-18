package fr.atlasworld.event.core.listener;

import com.google.common.base.Preconditions;
import fr.atlasworld.common.exception.NotImplementedException;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.executor.EventExecutor;
import fr.atlasworld.event.api.listener.EventListenerBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ListenerSettings<E extends Event> {
    private final EventExecutor executor;
    private final Consumer<Throwable> failureHandler;
    private final Predicate<E> filter;
    private final AtomicInteger expireCount;

    private ListenerSettings(EventExecutor executor, Consumer<Throwable> failureHandler, Predicate<E> filter, int expireCount) {
        this.executor = executor;
        this.failureHandler = failureHandler;
        this.filter = filter;
        this.expireCount = new AtomicInteger(expireCount);
    }

    public EventExecutor executor() {
        return this.executor;
    }

    public Consumer<Throwable> failureHandler() {
        return this.failureHandler;
    }

    public Predicate<E> filter() {
        return this.filter;
    }

    public int expireCount() {
        return this.expireCount.get();
    }

    public static class Builder<E extends Event> implements EventListenerBuilder<E> {
        private EventExecutor executor;
        private Consumer<Throwable> failureHandler;
        private Predicate<E> filter;
        private int expireCount;

        public Builder() {
            this.failureHandler = cause -> {};
            this.filter = cause -> true;
            this.expireCount = 0;
        }

        @Override
        public @NotNull EventListenerBuilder<E> executor(@NotNull EventExecutor executor) {
            Preconditions.checkNotNull(executor);

            this.executor = executor;
            return this;
        }

        @Override
        public @NotNull EventListenerBuilder<E> failure(@NotNull Consumer<Throwable> failureHandler) {
            Preconditions.checkNotNull(failureHandler);

            this.failureHandler = failureHandler;
            return this;
        }

        @Override
        public @NotNull EventListenerBuilder<E> filter(@NotNull Predicate<E> filter) {
            Preconditions.checkNotNull(filter);

            this.filter = filter;
            return this;
        }

        @Override
        public @NotNull EventListenerBuilder<E> expireCount(int executions) {
            this.expireCount = Math.max(executions, 0);
            return this;
        }

        @Override
        public @NotNull EventListenerBuilder<E> expireWhen(@NotNull Predicate<E> condition) {
            throw new NotImplementedException();
        }

        @Override
        public @NotNull <T extends Event> EventListenerBuilder<E> expireWhen(@NotNull Class<T> eventType) {
            throw new NotImplementedException();
        }

        @Override
        public @NotNull <T extends Event> EventListenerBuilder<E> expireWhen(@NotNull Class<T> eventType, @NotNull Consumer<T> condition) {
            throw new NotImplementedException();
        }

        public ListenerSettings<E> build() {
            if (this.executor == null)
                throw new IllegalStateException("No event executor set.");

            return new ListenerSettings<>(this.executor, this.failureHandler, this.filter, this.expireCount);
        }
    }
}
