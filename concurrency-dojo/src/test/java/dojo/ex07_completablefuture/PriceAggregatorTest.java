package dojo.ex07_completablefuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceAggregatorTest {

    private static Supplier<Double> slowValue(double value, long sleepMillis) {
        return () -> {
            sleep(sleepMillis);
            return value;
        };
    }

    private static Supplier<Double> slowFailure(long sleepMillis) {
        return () -> {
            sleep(sleepMillis);
            throw new RuntimeException("supplier unavailable");
        };
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void runsSuppliersConcurrentlyAndIgnoresFailures() {
        PriceAggregator aggregator = new PriceAggregator();

        List<Supplier<Double>> suppliers = List.of(
                slowValue(120.0, 300),
                slowFailure(300),
                slowValue(99.5, 300),   // the winner
                slowValue(150.0, 300),
                () -> { throw new RuntimeException("immediate failure"); }
        );

        long start = System.nanoTime();
        double best = aggregator.fetchBestPrice(suppliers);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(99.5, best, 0.0001);
        assertTrue(elapsedMs < 900,
                "took " + elapsedMs + "ms — suppliers were likely queried sequentially instead of concurrently");
    }

    @Test
    void throwsWhenEverySupplierFails() {
        PriceAggregator aggregator = new PriceAggregator();
        List<Supplier<Double>> suppliers = List.of(
                () -> { throw new RuntimeException("down"); },
                () -> { throw new RuntimeException("down"); }
        );

        assertThrows(IllegalStateException.class, () -> aggregator.fetchBestPrice(suppliers));
    }
}
