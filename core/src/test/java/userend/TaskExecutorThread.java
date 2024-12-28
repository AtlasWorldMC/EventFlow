package userend;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import fr.atlasworld.event.api.EventNode;
import org.jetbrains.annotations.NotNull;
import userend.event.TaskCompleteEvent;
import userend.event.TaskEvent;
import userend.event.TaskScheduledEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskExecutorThread extends Thread {
    private final BlockingQueue<Runnable> tasks;
    private final EventNode<TaskEvent> eventNode;

    private boolean running;

    public TaskExecutorThread(String name) {
        super(name);
        this.running = true;

        this.eventNode = Main.rootNode.createChildNode(this.getName(), TaskEvent.class, event -> event.executor() == this);
        this.tasks = new LinkedBlockingQueue<>();
    }

    public TaskExecutorThread(String name, int capacity) {
        super(name);
        this.running = true;

        this.eventNode = Main.rootNode.createChildNode(this.getName(), TaskEvent.class, event -> event.executor() == this);
        this.tasks = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void run() {

        Runnable task = null;
        Throwable cause = null;
        Stopwatch watch = Stopwatch.createUnstarted();

        try {
            while (this.running) {
                watch.reset();
                watch.start();

                task = this.tasks.take();

                try {
                    task.run();
                } catch (Throwable ex) {
                    cause = ex;
                }
                watch.stop();

                TaskCompleteEvent event = new TaskCompleteEvent(this, cause, watch.elapsed());
                Main.rootNode.callEvent(event);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        super.start();
    }

    public void shutdown(boolean interrupt) {
        this.running = false;

        if (interrupt)
            super.interrupt();
    }

    @Override
    public void interrupt() {
        this.shutdown(true);
    }

    public EventNode<TaskEvent> eventNode() {
        return this.eventNode;
    }

    /**
     * Queues a new task.
     *
     * @param runnable task to be executed.
     *
     * @return true if the task was successfully added, false otherwise.
     */
    public boolean queue(@NotNull Runnable runnable) {
        Preconditions.checkNotNull(runnable, "Task cannot be null!");

        TaskScheduledEvent event = new TaskScheduledEvent(Thread.currentThread(), this);
        Main.rootNode.callEvent(event).join();

        if (event.cancelled())
            return false;

        try {
            this.tasks.add(runnable);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
