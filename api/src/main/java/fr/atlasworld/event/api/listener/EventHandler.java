package fr.atlasworld.event.api.listener;

import fr.atlasworld.event.api.Event;

@FunctionalInterface
public interface EventHandler<E extends Event> {

    /**
     * Handles the event.
     *
     * @param event event called.
     *
     * @throws Throwable if something went wrong.
     */
    void handle(E event) throws Throwable;
}
