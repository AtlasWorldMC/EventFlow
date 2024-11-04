package unit;

import fr.atlasworld.event.api.Event;
import fr.atlasworld.event.api.annotation.EventHandler;
import fr.atlasworld.event.api.executor.EventExecutor;
import fr.atlasworld.event.api.listener.EventListener;
import fr.atlasworld.event.core.EventNodeImpl;
import fr.atlasworld.event.core.listener.ListenerSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class EventListenerTests {
    private static class TestEvent implements Event {}

    private EventNodeImpl<TestEvent> eventNode;

    @BeforeEach
    public void setUp() {
        eventNode = new EventNodeImpl<>("TestNode", TestEvent.class, null);
    }

    @Test
    @DisplayName("Test adding and executing a lambda listener")
    public void testAddAndExecuteLambdaListener() {
        AtomicInteger counter = new AtomicInteger(0);

        eventNode.addListener(TestEvent.class, event -> counter.incrementAndGet(), builder ->
                builder.executor(EventExecutor.syncExecutor)
        );

        eventNode.callEvent(new TestEvent()).onSuccess(event -> {
            assertEquals(1, counter.get(), "Lambda listener should be executed once.");
        });
    }

    @Test
    @DisplayName("Test adding and executing a method listener")
    public void testAddAndExecuteMethodListener() {
        TestListener listener = new TestListener();
        eventNode.addListener(listener, builder -> builder.executor(EventExecutor.syncExecutor));

        eventNode.callEvent(new TestEvent()).onSuccess(event -> {
            assertTrue(listener.isCalled(), "Method listener should be executed once.");
        });
    }

    @Test
    @DisplayName("Test listener expiration")
    public void testListenerExpiration() {
        AtomicInteger counter = new AtomicInteger(0);

        eventNode.addListener(TestEvent.class, event -> counter.incrementAndGet(), builder -> builder
                .expireCount(1)
                .executor(EventExecutor.syncExecutor)
                .failure(Throwable::printStackTrace)
        );

        eventNode.callEvent(new TestEvent()).onSuccess(event -> {
            assertEquals(1, counter.get(), "Listener should be executed once.");
        });

        eventNode.callEvent(new TestEvent()).onSuccess(event -> {
            assertEquals(1, counter.get(), "Expired listener should not be executed again.");
        });
    }

    @Test
    @DisplayName("Test filtering listeners")
    public void testFilterListeners() {
        AtomicInteger counter = new AtomicInteger(0);
        eventNode.addListener(TestEvent.class, event -> counter.incrementAndGet(), builder -> builder
                .filter(event -> true)
                .expireCount(1)
                .executor(EventExecutor.syncExecutor)
        );

        eventNode.callEvent(new TestEvent()).onSuccess(event -> {
            assertEquals(1, counter.get(), "Listener should be executed when filter passes.");
        });

        counter.set(0);
        eventNode.addListener(TestEvent.class, event -> counter.incrementAndGet(), builder -> builder
                .filter(event -> false)
                .expireCount(1)
                .executor(EventExecutor.syncExecutor)
        );

        eventNode.callEvent(new TestEvent()).onSuccess(event -> {
            assertEquals(0, counter.get(), "Listener should not be executed when filter fails.");
        });
    }

    @Test
    @DisplayName("Test handling exceptions in listeners")
    public void testHandleExceptionsInListeners() {
        AtomicBoolean called = new AtomicBoolean(false);
        Consumer<Throwable> failureHandler = throwable -> {};
        ListenerSettings<TestEvent> settings = new ListenerSettings.Builder<TestEvent>()
                .executor(EventExecutor.syncExecutor)
                .failure(failureHandler)
                .build();

        eventNode.addListener(TestEvent.class, event -> { throw new RuntimeException("Test exception"); }, builder ->
                builder.executor(EventExecutor.syncExecutor));
        eventNode.callEvent(new TestEvent()).onFailure(throwable -> {
            called.set(true);
        }).syncUninterruptibly();

        assertTrue(called.get(), "Thrown exception should be returned in exception.");
    }

    private static class TestListener implements EventListener {
        private boolean called = false;

        @EventHandler
        private void onEvent(TestEvent event) {
            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }
}
