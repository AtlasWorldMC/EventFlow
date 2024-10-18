package fr.atlasworld.event.core.listener;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.listener.EventListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class MethodRegisteredListener<E extends Event> extends RegisteredListener<E> {
    private final EventListener instance;
    private final Method method;

    public MethodRegisteredListener(ListenerSettings<E> settings, EventListener instance, Method method) {
        super(settings);
        this.instance = instance;
        this.method = method;
    }

    @Override
    public void run(@NotNull Event event) {
        try {
            this.method.invoke(this.instance, event);
        } catch (Throwable cause) {
            this.handleException(cause);
        }
    }
}
