package dojo.ex05_rwlock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputeCacheTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void slowLoaderIsInvokedAtMostOnceUnderConcurrentAccessForTheSameKey() throws Exception {
        final int threads = 40;
        ComputeCache<String, String> cache = new ComputeCache<>();
        AtomicInteger loaderCalls = new AtomicInteger();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        startGate.await();
                        String value = cache.get("shared-key", k -> {
                            loaderCalls.incrementAndGet();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "computed-" + k;
                        });
                        assertEquals("computed-shared-key", value);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneGate.countDown();
                    }
                });
            }

            long start = System.nanoTime();
            startGate.countDown();
            assertTrue(doneGate.await(10, TimeUnit.SECONDS), "threads did not finish in time");
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            assertEquals(1, loaderCalls.get(),
                    "loader was invoked more than once for the same key — cache stampede detected");
            assertTrue(elapsedMs < 2000,
                    "took too long (" + elapsedMs + "ms) — looks like requests were serialized instead of "
                            + "only the single loader call blocking");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void differentKeysComputeIndependently() {
        ComputeCache<Integer, Integer> cache = new ComputeCache<>();
        for (int i = 0; i < 100; i++) {
            int expected = i * i;
            assertEquals(expected, cache.get(i, k -> k * k));
        }
        assertEquals(100, cache.size());

        // second read for an already-present key must not recompute
        AtomicInteger recomputed = new AtomicInteger();
        int value = cache.get(5, k -> {
            recomputed.incrementAndGet();
            return -1;
        });
        assertEquals(25, value);
        assertEquals(0, recomputed.get(), "get() recomputed a value that was already cached");
    }
}
