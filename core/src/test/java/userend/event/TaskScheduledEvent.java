package userend.event;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import userend.TaskExecutorThread;

/**
 * Event Called when a task has been scheduled.
 */
public class TaskScheduledEvent implements TaskEvent {
    private final Thread originThread;
    private final TaskExecutorThread executor;

    private boolean cancelled;

    public TaskScheduledEvent(@NotNull Thread originThread, @NotNull TaskExecutorThread executor) {
        Preconditions.checkNotNull(originThread);
        Preconditions.checkNotNull(executor);

        this.originThread = originThread;
        this.executor = executor;
    }

    public @NotNull Thread originThread() {
        return this.originThread;
    }

    @Override
    public @NotNull TaskExecutorThread executor() {
        return this.executor;
    }

    public boolean cancelled() {
        return this.cancelled;
    }

    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
