package dojo.ex04_producerconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedBufferTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void putBlocksWhileFullAndTakeBlocksWhileEmpty() throws Exception {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(1);
        buffer.put(1);

        AtomicBoolean secondPutReturned = new AtomicBoolean(false);
        Thread producer = new Thread(() -> {
            try {
                buffer.put(2);
                secondPutReturned.set(true);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();

        Thread.sleep(300);
        assertFalse(secondPutReturned.get(), "put() should block while the buffer is full");

        assertEquals(1, buffer.take());
        producer.join(3000);
        assertTrue(secondPutReturned.get(), "put() should have unblocked once space was freed");
        assertEquals(2, buffer.take());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void manyProducersAndConsumersExchangeAllItemsExactlyOnce() throws Exception {
        final int capacity = 8;
        final int producers = 6;
        final int consumers = 6;
        final int itemsPerProducer = 5_000;
        final long totalItems = (long) producers * itemsPerProducer;

        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(capacity);
        AtomicLong producedCount = new AtomicLong();
        AtomicLong consumedCount = new AtomicLong();
        AtomicLong consumedSum = new AtomicLong();

        ExecutorService pool = Executors.newFixedThreadPool(producers + consumers);
        try {
            for (int p = 0; p < producers; p++) {
                pool.submit(() -> {
                    try {
                        for (int j = 0; j < itemsPerProducer; j++) {
                            buffer.put(1);
                            producedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            for (int c = 0; c < consumers; c++) {
                pool.submit(() -> {
                    try {
                        while (consumedCount.get() < totalItems) {
                            long already = consumedCount.get();
                            if (already >= totalItems) {
                                return;
                            }
                            Integer v = buffer.take();
                            consumedSum.addAndGet(v);
                            consumedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            long deadline = System.currentTimeMillis() + 25_000;
            while (consumedCount.get() < totalItems && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(totalItems, producedCount.get(), "not all items were produced");
        assertEquals(totalItems, consumedCount.get(), "not all items were consumed — possible deadlock or lost wakeup");
        assertEquals(totalItems, consumedSum.get(), "item values don't add up — data was lost or duplicated");
    }
}
