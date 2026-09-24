package dojo.ex08_virtualthreads;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelFetcherTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runsTasksConcurrentlyAndPreservesOrder() throws Exception {
        ParallelFetcher fetcher = new ParallelFetcher();

        List<Callable<String>> tasks = List.of(
                slowTask("a", 300),
                slowTask("b", 300),
                slowTask("c", 300),
                slowTask("d", 300),
                slowTask("e", 300)
        );

        long start = System.nanoTime();
        List<String> results = fetcher.fetchAll(tasks);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(List.of("a", "b", "c", "d", "e"), results, "input order was not preserved");
        assertTrue(elapsedMs < 900,
                "took " + elapsedMs + "ms — tasks were likely run sequentially instead of concurrently");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void cancelsOutstandingTasksAssoonAsOneFails() throws Exception {
        ParallelFetcher fetcher = new ParallelFetcher();
        AtomicBoolean longTaskCompletedNormally = new AtomicBoolean(false);

        Callable<String> failsFast = () -> {
            Thread.sleep(100);
            throw new IllegalStateException("boom");
        };
        Callable<String> longRunning = () -> {
            try {
                Thread.sleep(5000);
                longTaskCompletedNormally.set(true);
                return "should not get here";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        };

        List<Callable<String>> tasks = List.of(longRunning, failsFast, longRunning);

        long start = System.nanoTime();
        Exception thrown = assertThrows(Exception.class, () -> fetcher.fetchAll(tasks));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMs < 2000,
                "took " + elapsedMs + "ms — fetchAll() should fail fast instead of waiting for the "
                        + "long-running tasks");
        assertFalse(longTaskCompletedNormally.get(),
                "long-running task completed normally — it should have been cancelled");

        Throwable cause = thrown.getCause() != null ? thrown.getCause() : thrown;
        assertTrue(cause instanceof IllegalStateException || thrown instanceof IllegalStateException,
                "expected the original failure to be propagated, got: " + thrown);
    }

    private static Callable<String> slowTask(String value, long sleepMillis) {
        return () -> {
            Thread.sleep(sleepMillis);
            return value;
        };
    }
}
