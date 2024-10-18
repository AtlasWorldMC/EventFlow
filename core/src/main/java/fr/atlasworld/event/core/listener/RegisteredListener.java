package fr.atlasworld.event.core.listener;

import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.common.concurrent.action.SimpleFutureAction;
import fr.atlasworld.event.api.Event;
import org.jetbrains.annotations.NotNull;

public abstract class RegisteredListener<E extends Event> {
    private final ListenerSettings<E> settings;

    protected RegisteredListener(ListenerSettings<E> settings) {
        this.settings = settings;
    }

    public abstract void run(@NotNull E event);

    protected final void handleException(Throwable cause) {
        this.settings.failureHandler().accept(cause);
    }

    public FutureAction<E> callEvent(@NotNull E event) {
        SimpleFutureAction<E> future = new SimpleFutureAction<>();

        try {
            this.settings.executor().request(() -> this.run(event))
                    .onSuccess(unused -> future.complete(event))
                    .onFailure(future::fail); // Proxying future.
        } catch (InterruptedException e) {
            future.fail(e);
        }

        return future;
    }
}
