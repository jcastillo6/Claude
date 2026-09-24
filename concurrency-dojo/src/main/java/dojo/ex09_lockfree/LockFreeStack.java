package dojo.ex09_lockfree;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Exercise 09 — Lock-free data structures (Treiber stack).
 *
 * Implement push/pop using only compare-and-set on an AtomicReference —
 * no synchronized, no explicit java.util.concurrent.locks.Lock. This is
 * the classic optimistic-concurrency pattern: read the current state,
 * compute the desired next state, then atomically swap only if nobody
 * else beat you to it; retry on failure.
 */
public class LockFreeStack<T> {

    private static final class Node<T> {
        final T value;
        final Node<T> next;

        Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    private final AtomicReference<Node<T>> top = new AtomicReference<>();

    /**
     * Pushes a value onto the stack. Must be lock-free and safe under
     * unbounded concurrent pushers and poppers.
     */
    public void push(T value) {
        // TODO: implement using a CAS retry loop.
        throw new UnsupportedOperationException("TODO: implement push()");
    }

    /**
     * Pops and returns the top value, or returns null if the stack is
     * empty. Must be lock-free and safe under unbounded concurrent
     * pushers and poppers.
     */
    public T pop() {
        // TODO: implement using a CAS retry loop.
        throw new UnsupportedOperationException("TODO: implement pop()");
    }

    public boolean isEmpty() {
        return top.get() == null;
    }
}
