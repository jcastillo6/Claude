package dojo.ex02_atomicity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnsafeCounterTest {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void survivesHeavyConcurrentIncrement() throws InterruptedException {
        final int threads = 16;
        final int incrementsPerThread = 200_000;

        UnsafeCounter counter = new UnsafeCounter();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        startGate.await();
                        for (int j = 0; j < incrementsPerThread; j++) {
                            counter.increment();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneGate.countDown();
                    }
                });
            }

            startGate.countDown(); // release all threads at once to maximize contention
            assertTrue(doneGate.await(15, TimeUnit.SECONDS), "threads did not finish in time");
        } finally {
            pool.shutdownNow();
        }

        assertEquals((long) threads * incrementsPerThread, counter.get(),
                "lost updates detected — increment() is not safe under concurrent invocation");
    }
}
