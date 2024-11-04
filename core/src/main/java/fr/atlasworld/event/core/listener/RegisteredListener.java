package fr.atlasworld.event.core.listener;

import fr.atlasworld.common.concurrent.action.FutureAction;
import fr.atlasworld.common.concurrent.action.SimpleFutureAction;
import fr.atlasworld.event.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RegisteredListener<E extends Event> {
    private final ListenerSettings<E> settings;

    private final AtomicBoolean expired;

    protected RegisteredListener(ListenerSettings<E> settings) {
        this.settings = settings;
        this.expired = new AtomicBoolean(false);
    }

    public abstract void run(@NotNull E event) throws Throwable;

    protected final void handleException(Throwable cause) {
        this.settings.failureHandler().accept(cause);
    }

    public boolean isExpired(E event) {
        if (this.expired.get())
            return true;

        if (this.settings.expired(event)) {
            this.expired.set(true);
            return true;
        }

        return false;
    }

    public FutureAction<E> callEvent(@NotNull E event) {
        SimpleFutureAction<E> future = new SimpleFutureAction<>();

        if (this.expired.get() && !this.settings.testEvent(event))
            return future.complete(null);

        try {
            this.settings.executor().request(() -> this.run(event))
                    .onSuccess(unused -> future.complete(event))
                    .onFailure(cause -> {
                        this.handleException(cause);
                        future.fail(cause);
                    }); // Proxying future.
        } catch (InterruptedException e) {
            future.fail(e);
        }

        return future;
    }
}
