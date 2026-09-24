package dojo.ex07_completablefuture;

import java.util.List;
import java.util.function.Supplier;

/**
 * Exercise 07 — Composing async work with CompletableFuture.
 *
 * TODO: implement fetchBestPrice() so that:
 *  - all suppliers are queried CONCURRENTLY, not one after another (the
 *    test asserts on total wall-clock time to catch a sequential
 *    implementation).
 *  - a supplier that throws must not fail the whole aggregation — just
 *    exclude that supplier's result.
 *  - the return value is the minimum price among the suppliers that
 *    succeeded.
 *  - if every supplier fails, throw IllegalStateException.
 */
public class PriceAggregator {

    public double fetchBestPrice(List<Supplier<Double>> suppliers) {
        // TODO: implement using CompletableFuture.supplyAsync + composition.
        throw new UnsupportedOperationException("TODO: implement fetchBestPrice()");
    }
}
