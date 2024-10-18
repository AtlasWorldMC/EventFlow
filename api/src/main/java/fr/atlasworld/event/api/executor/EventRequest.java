package fr.atlasworld.event.api.executor;

import org.jetbrains.annotations.Blocking;

/**
 * Event request, an abstract way to represent a listener and its event that can be executed.
 */
public interface EventRequest {

    /**
     * Executes the request on the current thread.
     * <br>
     * This will <b>block</b> the current thread the time that the event is being handled by the current listener.
     */
    @Blocking
    void execute();
}
