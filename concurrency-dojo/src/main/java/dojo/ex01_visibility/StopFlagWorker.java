package dojo.ex01_visibility;

/**
 * Exercise 01 — Visibility.
 *
 * Under the Java Memory Model, a write made by one thread is not guaranteed
 * to ever become visible to another thread unless a happens-before edge
 * connects the write and the read (a volatile write/read pair, a lock
 * release/acquire, thread start/join, etc). Without such an edge, the
 * compiler and CPU are free to cache {@code running} in a register and the
 * reader thread may spin forever even after another thread sets it to
 * false.
 *
 * TODO: fix the bug WITHOUT introducing a lock — this field just needs the
 * right modifier to establish the happens-before edge between stop() and
 * the loop condition in run().
 */
public class StopFlagWorker implements Runnable {

    // TODO: this field needs a modifier to guarantee cross-thread visibility.
    private boolean running = true;

    private volatile long iterations = 0;

    @Override
    public void run() {
        while (running) {
            iterations++;
        }
    }

    /** Signals the worker to stop. May be called from a different thread. */
    public void stop() {
        running = false;
    }

    /** Best-effort progress counter, safe to read from any thread. */
    public long getIterations() {
        return iterations;
    }
}
