package dojo.ex10_semaphore;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Exercise 10 — Bounding concurrency with a Semaphore.
 *
 * TODO: implement withResource() so that at most {@code maxConcurrent}
 * invocations of {@code task} run at the same time across ALL callers
 * (imagine {@code task} represents access to a limited pool of DB
 * connections or file handles). Additional callers must block until a
 * permit frees up. A permit must be released even if {@code task}
 * throws — a leaked permit would permanently shrink the pool's capacity.
 */
public class ResourcePool<T> {

    private final Semaphore semaphore;

    public ResourcePool(int maxConcurrent) {
        if (maxConcurrent <= 0) {
            throw new IllegalArgumentException("maxConcurrent must be positive");
        }
        this.semaphore = new Semaphore(maxConcurrent);
    }

    public <R> R withResource(Supplier<R> task) {
        // TODO: implement using acquire()/release() with a try/finally.
        throw new UnsupportedOperationException("TODO: implement withResource()");
    }
}
