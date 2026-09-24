package dojo.ex10_semaphore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePoolTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void neverExceedsMaxConcurrentUsage() throws InterruptedException {
        final int maxConcurrent = 3;
        final int callers = 30;

        ResourcePool<Void> pool = new ResourcePool<>(maxConcurrent);
        AtomicInteger current = new AtomicInteger();
        AtomicInteger observedMax = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(callers);
        try {
            for (int i = 0; i < callers; i++) {
                executor.submit(() -> pool.withResource(() -> {
                    int now = current.incrementAndGet();
                    observedMax.updateAndGet(prev -> Math.max(prev, now));
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        current.decrementAndGet();
                    }
                    return null;
                }));
            }
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }

        assertTrue(observedMax.get() <= maxConcurrent,
                "observed " + observedMax.get() + " concurrent executions, exceeding the limit of " + maxConcurrent);
        assertEquals(maxConcurrent, observedMax.get(),
                "pool never reached full utilization — double check it isn't over-restricting concurrency");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void permitIsReleasedEvenWhenTaskThrows() throws InterruptedException {
        ResourcePool<Void> pool = new ResourcePool<>(2);

        for (int i = 0; i < 5; i++) {
            assertThrows(RuntimeException.class, () -> pool.withResource(() -> {
                throw new RuntimeException("boom");
            }));
        }

        // If permits leaked above, this batch (which needs full capacity)
        // would hang and the @Timeout would fail the test.
        AtomicInteger current = new AtomicInteger();
        AtomicInteger observedMax = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 2; i++) {
                executor.submit(() -> pool.withResource(() -> {
                    int now = current.incrementAndGet();
                    observedMax.updateAndGet(prev -> Math.max(prev, now));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        current.decrementAndGet();
                    }
                    return null;
                }));
            }
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }

        assertEquals(2, observedMax.get(), "permits appear to have leaked after a task threw");
    }
}
