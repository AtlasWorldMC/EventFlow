package userend.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import userend.TaskExecutorThread;

import java.time.Duration;

/**
 * Called when a task has completed execution.
 */
public class TaskCompleteEvent implements TaskEvent {
    private final TaskExecutorThread executor;
    private final @Nullable Throwable cause;
    private final Duration executionTime;

    public TaskCompleteEvent(@NotNull TaskExecutorThread executor, @Nullable Throwable cause, @NotNull Duration executionTime) {
        this.executor = executor;
        this.cause = cause;
        this.executionTime = executionTime;
    }

    @Override
    public @NotNull TaskExecutorThread executor() {
        return this.executor;
    }

    public boolean taskSuccessful() {
        return this.cause == null;
    }

    @Nullable
    public Throwable cause() {
        return this.cause;
    }

    @NotNull
    public Duration executionTime() {
        return this.executionTime;
    }
}
