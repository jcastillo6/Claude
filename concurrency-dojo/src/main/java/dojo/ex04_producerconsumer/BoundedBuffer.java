package dojo.ex04_producerconsumer;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Exercise 04 — Producer/consumer coordination with wait()/notifyAll().
 *
 * Implement a classic bounded buffer using intrinsic locking and the
 * Object monitor methods directly (do NOT use java.util.concurrent's
 * BlockingQueue — that would defeat the point of this exercise).
 *
 * Requirements:
 *  - put() blocks while the buffer is full, without busy-waiting.
 *  - take() blocks while the buffer is empty, without busy-waiting.
 *  - Both must be correct under multiple concurrent producers AND
 *    multiple concurrent consumers (a single notify() is not enough —
 *    think about why, and prefer notifyAll(), or model this with two
 *    condition predicates).
 *  - The buffer must never exceed its configured capacity.
 */
public class BoundedBuffer<T> {

    private final Deque<T> queue = new ArrayDeque<>();
    private final int capacity;

    public BoundedBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    /**
     * Blocks until there is room, then adds {@code item}.
     */
    public synchronized void put(T item) throws InterruptedException {
        // TODO: implement using wait()/notifyAll() — no busy-waiting.
        throw new UnsupportedOperationException("TODO: implement put()");
    }

    /**
     * Blocks until an item is available, then removes and returns it.
     */
    public synchronized T take() throws InterruptedException {
        // TODO: implement using wait()/notifyAll() — no busy-waiting.
        throw new UnsupportedOperationException("TODO: implement take()");
    }

    public synchronized int size() {
        return queue.size();
    }
}
