package dojo.ex08_virtualthreads;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Exercise 08 — Fan-out/fan-in with virtual threads, structured-concurrency
 * style, implemented by hand with a plain ExecutorService.
 *
 * TODO: implement fetchAll() so that:
 *  - every task runs concurrently on a virtual thread (use
 *    Executors.newVirtualThreadPerTaskExecutor()).
 *  - the returned list preserves the INPUT order of {@code tasks}.
 *  - if ANY task throws, the still-running tasks are cancelled
 *    (interrupted) as soon as possible, and the original exception is
 *    rethrown wrapped in a RuntimeException — don't wait for every task
 *    to finish first.
 *  - the executor is always shut down / closed, even on failure (no
 *    thread leak).
 */
public class ParallelFetcher {

    public List<String> fetchAll(List<Callable<String>> tasks) throws Exception {
        // TODO: implement fan-out/fan-in with cancellation-on-failure.
        throw new UnsupportedOperationException("TODO: implement fetchAll()");
    }
}
