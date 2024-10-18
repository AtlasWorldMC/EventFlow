package fr.atlasworld.event.core.listener;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.listener.EventHandler;
import org.jetbrains.annotations.NotNull;

public class LambdaRegisteredListener<E extends Event> extends RegisteredListener<E> {
    private final EventHandler<E> handler;

    public LambdaRegisteredListener(ListenerSettings<E> settings, EventHandler<E> handler) {
        super(settings);
        this.handler = handler;
    }

    @Override
    public void run(@NotNull E event) {
        try {
            this.handler.handle(event);
        } catch (Throwable cause) {
            this.handleException(cause);
        }
    }
}
