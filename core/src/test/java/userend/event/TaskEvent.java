package userend.event;

import fr.atlasworld.event.api.Event;
import org.jetbrains.annotations.NotNull;
import userend.TaskExecutorThread;

/**
 * Execution Events
 */
public interface TaskEvent extends Event {

    /**
     * Retrieve the executor.
     *
     * @return task executor.
     */
    @NotNull
    TaskExecutorThread executor();
}
