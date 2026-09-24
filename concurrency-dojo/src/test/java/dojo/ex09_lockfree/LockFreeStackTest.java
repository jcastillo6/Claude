package dojo.ex09_lockfree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockFreeStackTest {

    @Test
    void popOnEmptyStackReturnsNull() {
        LockFreeStack<String> stack = new LockFreeStack<>();
        assertNull(stack.pop());
    }

    @Test
    void singleThreadedPushPopIsLIFO() {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);

        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertNull(stack.pop());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void everyPushedValueIsPoppedExactlyOnceUnderConcurrency() throws InterruptedException {
        final int pushers = 8;
        final int itemsPerPusher = 20_000;
        final int total = pushers * itemsPerPusher;

        LockFreeStack<Integer> stack = new LockFreeStack<>();

        // Phase 1: concurrent pushes of unique values 0..total-1.
        ExecutorService pushPool = Executors.newFixedThreadPool(pushers);
        try {
            for (int p = 0; p < pushers; p++) {
                final int base = p * itemsPerPusher;
                pushPool.submit(() -> {
                    for (int i = 0; i < itemsPerPusher; i++) {
                        stack.push(base + i);
                    }
                });
            }
            pushPool.shutdown();
            assertTrue(pushPool.awaitTermination(15, TimeUnit.SECONDS), "pushes did not finish in time");
        } finally {
            pushPool.shutdownNow();
        }

        // Phase 2: concurrent pops racing to drain the stack.
        AtomicIntegerArray seenCount = new AtomicIntegerArray(total);
        AtomicLong poppedTotal = new AtomicLong();
        int poppers = 8;
        ExecutorService popPool = Executors.newFixedThreadPool(poppers);
        try {
            for (int c = 0; c < poppers; c++) {
                popPool.submit(() -> {
                    Integer value;
                    while ((value = stack.pop()) != null) {
                        seenCount.incrementAndGet(value);
                        poppedTotal.incrementAndGet();
                    }
                });
            }
            popPool.shutdown();
            assertTrue(popPool.awaitTermination(15, TimeUnit.SECONDS), "pops did not finish in time");
        } finally {
            popPool.shutdownNow();
        }

        assertEquals(total, poppedTotal.get(), "wrong number of items popped — items were lost or duplicated");
        for (int i = 0; i < total; i++) {
            assertEquals(1, seenCount.get(i), "value " + i + " was popped " + seenCount.get(i) + " times");
        }
        assertTrue(stack.isEmpty());
    }
}
