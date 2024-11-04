package fr.atlasworld.event.core.listener;

import com.google.common.base.Preconditions;
import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.executor.EventExecutor;
import fr.atlasworld.event.api.listener.EventListenerBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ListenerSettings<E extends Event> {
    private final EventExecutor executor;
    private final Consumer<Throwable> failureHandler;
    private final AtomicInteger expireCount;

    private final List<Predicate<E>> filters;
    private final List<Predicate<E>> expireConditions;

    private final boolean countExpires;

    private ListenerSettings(EventExecutor executor, Consumer<Throwable> failureHandler, int expireCount,
                             List<Predicate<E>> filters, List<Predicate<E>> expireConditions) {

        this.executor = executor;
        this.failureHandler = failureHandler;
        this.expireCount = new AtomicInteger(expireCount);

        this.countExpires = expireCount > 0;

        this.filters = Collections.unmodifiableList(filters);
        this.expireConditions = Collections.unmodifiableList(expireConditions);
    }

    public EventExecutor executor() {
        return this.executor;
    }

    public Consumer<Throwable> failureHandler() {
        return this.failureHandler;
    }

    public boolean testEvent(E event) {
        for (Predicate<E> filter : this.filters) {
            if (!filter.test(event))
                return false;
        }

        return true;
    }

    public boolean expired(E event) {
        for (Predicate<E> condition : this.expireConditions) {
            if (condition.test(event))
                return true;
        }

        if (this.countExpires) {
            return this.expireCount.getAndDecrement() <= 0;
        }

        return false;
    }

    public static class Builder<E extends Event> implements EventListenerBuilder<E> {
        private EventExecutor executor;
        private Consumer<Throwable> failureHandler;
        private int expireCount;

        private final List<Predicate<E>> filter;
        private final List<Predicate<E>> expireConditions;

        public Builder() {
            this.failureHandler = cause -> {};
            this.expireCount = 0;

            this.filter = new ArrayList<>();
            this.expireConditions = new ArrayList<>();
        }

        @Override
        public @NotNull Builder<E> executor(@NotNull EventExecutor executor) {
            Preconditions.checkNotNull(executor);

            this.executor = executor;
            return this;
        }

        @Override
        public @NotNull Builder<E> failure(@NotNull Consumer<Throwable> failureHandler) {
            Preconditions.checkNotNull(failureHandler);

            this.failureHandler = failureHandler;
            return this;
        }

        @Override
        public @NotNull Builder<E> filter(@NotNull Predicate<E> filter) {
            Preconditions.checkNotNull(filter);

            this.filter.add(filter);
            return this;
        }

        @Override
        public @NotNull Builder<E> expireCount(int executions) {
            this.expireCount = Math.max(executions, 0);
            return this;
        }

        @Override
        public @NotNull Builder<E> expireWhen(@NotNull Predicate<E> condition) {
            Preconditions.checkNotNull(condition);

            this.expireConditions.add(condition);
            return this;
        }

        public ListenerSettings<E> build() {
            if (this.executor == null)
                throw new IllegalStateException("No event executor set.");

            return new ListenerSettings<>(this.executor, this.failureHandler, this.expireCount, this.filter,
                    this.expireConditions);
        }
    }
}
