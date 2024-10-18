package userend;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.EventNode;
import userend.event.TaskCompleteEvent;
import userend.listener.TaskListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {
    public static final EventNode<Event> rootNode = EventNode.create("root");

    private static final List<TaskExecutorThread> threads = new ArrayList<>();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++)
            threads.add(new TaskExecutorThread("task-executor-" + i));

        threads.forEach(TaskExecutorThread::start);
        Random random = new Random();

        rootNode.addListener(new TaskListener(), builder -> {});

        int selectedIsolated = random.nextInt(0, threads.size());
        threads.get(selectedIsolated).eventNode().addListener(TaskCompleteEvent.class, event -> {
            System.out.println("ISOLATED #" + selectedIsolated + ": A Task finished on my executor!");
        }, builder -> {});

        while (true) {
            int executor = random.nextInt(0, threads.size());
            long nextQueue = random.nextLong(10000);

            threads.get(executor).queue(() -> System.out.println("Next task will be scheduled in " + nextQueue + "ms."));

            try {
                Thread.sleep(nextQueue);
            } catch (InterruptedException ignored) {

            }
        }
    }


}
