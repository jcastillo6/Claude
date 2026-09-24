package dojo.ex02_atomicity;

/**
 * Exercise 02 — Atomicity.
 *
 * {@code count++} is actually three operations (read, add, write) and is
 * not atomic. Under concurrent invocation, increments are silently lost.
 *
 * TODO: make this counter safe under concurrent invocation. Prefer a
 * lock-free approach (java.util.concurrent.atomic) over coarse locking if
 * you can — but either is acceptable as long as it is correct.
 */
public class UnsafeCounter {

    // TODO: replace with an atomic field, or add proper synchronization
    // around all accesses to this field.
    private long count = 0;

    public void increment() {
        // TODO: implement this so it is safe under concurrent invocation.
        throw new UnsupportedOperationException("TODO: implement increment()");
    }

    public long get() {
        // TODO: implement this so it observes a consistent value.
        throw new UnsupportedOperationException("TODO: implement get()");
    }
}
