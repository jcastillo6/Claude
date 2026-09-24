package dojo.ex05_rwlock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Exercise 05 — ReadWriteLock and the "cache stampede" problem.
 *
 * A naive cache guarded by a single lock serializes ALL reads, even for
 * keys that are already present — wasteful when reads vastly outnumber
 * writes. A naive cache with no synchronization at all can call the loader
 * multiple times concurrently for the same missing key ("stampede" / "dog
 * pile"), wasting work and, if the loader has side effects, potentially
 * corrupting state.
 *
 * TODO: implement get() so that:
 *  1. A cache HIT does not block other concurrent cache hits (readers
 *     don't block readers).
 *  2. A cache MISS invokes {@code loader} for a given key AT MOST ONCE,
 *     even if many threads race to request that same missing key at the
 *     same time. Racing threads must all receive the same computed value.
 *  3. The loader for one key must not block reads/hits for unrelated keys
 *     for the loader's whole duration (a brief moment of exclusion while
 *     publishing the result into the map is fine).
 */
public class ComputeCache<K, V> {

    private final Map<K, V> cache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public V get(K key, Function<K, V> loader) {
        // TODO: implement the double-checked read/write-lock pattern here.
        throw new UnsupportedOperationException("TODO: implement get()");
    }

    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
