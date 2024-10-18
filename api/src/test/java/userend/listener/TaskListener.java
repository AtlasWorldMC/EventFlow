package userend.listener;

import fr.atlasworld.event.api.annotation.EventHandler;
import fr.atlasworld.event.api.listener.EventListener;
import userend.event.TaskCompleteEvent;
import userend.event.TaskScheduledEvent;

import java.util.Random;

public class TaskListener implements EventListener {
    private final Random random = new Random();

    @EventHandler
    public void onTaskScheduled(TaskScheduledEvent event) {
        int selected = random.nextInt(0, 11);
        boolean cancelled = selected == 7;

        event.cancelled(cancelled);
        if (event.cancelled())
            System.out.println("Task on '" + event.executor().getName() + "' has been cancelled.");

        return;
    }

    @EventHandler
    public void onTaskCompleted(TaskCompleteEvent event) {
        System.out.println("Task on '" + event.executor().getName() + "' finished in " + event.executionTime().toMillis() + "ms.");
    }
}
