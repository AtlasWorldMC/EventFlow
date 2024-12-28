package fr.atlasworld.event.api.executor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an event executor, this allows event to be executed asynchronously.
 * <p>
 * This can be used to execute event in a queued executor,
 * by first queuing the event calls then execute them.
 */
public interface EventExecutor {

    /**
     * Request the execution of an {@link EventRequest}.
     *
     * @param request request to be executed.
     *
     * @return future that will be completed once the request has been executed.
     *
     * @throws InterruptedException when the requesting thread is interrupted.
     */
    CompletableFuture<Void> request(@NotNull EventRequest request) throws InterruptedException;

    /**
     * Synchronous executor,
     * This runs the listener on the same thread that called the event.
     * <p>
     * The usage of this executor is not recommended for systems where reactivity is key.
     * This could lead to much slower executions on event that have many listeners.
     */
    final EventExecutor syncExecutor = request -> {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            request.execute();
            future.complete(null);
        } catch (final Throwable e) {
            future.completeExceptionally(e);
        }

        return future;
    };
}
