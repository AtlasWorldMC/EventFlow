package fr.atlasworld.event.api.executor;

import fr.atlasworld.common.concurrent.action.FutureAction;
import org.jetbrains.annotations.NotNull;

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
    FutureAction<Void> request(@NotNull EventRequest request) throws InterruptedException;
}
