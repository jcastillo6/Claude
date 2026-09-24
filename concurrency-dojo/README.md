# Concurrency Dojo

A staff-level Java 25 concurrency practice project. Every exercise ships as
a **broken or unimplemented skeleton** in `src/main/java` plus a **stress
test** in `src/test/java` that only passes once you've implemented it
correctly. The tests aren't unit tests in the usual sense — they're
adversarial: high thread counts, tight timeouts, and assertions specifically
designed to catch the classic ways each concurrency primitive gets misused
(lost updates, lost wakeups, stampedes, deadlocks, leaked permits, thread
leaks, reordering).

## Getting started

```bash
mvn test                    # run everything
mvn test -Dtest=Ex02*       # run one exercise's tests
```

Work through the exercises in order — they build in difficulty and each
one's `README` note below explains the concept it's testing before you open
the code.

## Exercises

| # | Package | Concept | What's broken |
|---|---------|---------|----------------|
| 01 | `ex01_visibility` | Visibility / happens-before | A stop flag read in a loop with no synchronization — the JIT/CPU may never surface another thread's write. Fix: correct field modifier. |
| 02 | `ex02_atomicity` | Read-modify-write races | `count++` is three unsynchronized steps. Fix: `AtomicLong` or proper locking. |
| 03 | `ex03_singleton` | Double-checked locking, safe publication | A lazy singleton where a racing thread can observe a non-null but not-yet-fully-constructed reference. Fix: correct DCL idiom. |
| 04 | `ex04_producerconsumer` | Intrinsic locks, `wait()`/`notifyAll()` | A bounded buffer with no coordination logic. Implement blocking put/take without busy-waiting or lost wakeups. |
| 05 | `ex05_rwlock` | `ReadWriteLock`, cache stampede | A cache that must let concurrent hits proceed in parallel while guaranteeing a miss's loader runs at most once. |
| 06 | `ex06_deadlock` | Lock ordering | A funds transfer between two accounts, each guarded by its own lock — the naive implementation deadlocks under opposite-direction concurrent transfers. Fix: consistent lock ordering. |
| 07 | `ex07_completablefuture` | Async composition, failure isolation | Query several suppliers concurrently via `CompletableFuture`, tolerate individual failures, pick the best result. |
| 08 | `ex08_virtualthreads` | Structured fan-out/fan-in, cancellation | Run tasks on virtual threads, preserve order, cancel the rest as soon as one fails, never leak threads. |
| 09 | `ex09_lockfree` | Lock-free algorithms, CAS | A Treiber stack implemented with `AtomicReference` compare-and-set instead of locks. |
| 10 | `ex10_semaphore` | Bounding concurrency | A resource pool that must cap concurrent access via `Semaphore` and never leak a permit, even on exceptions. |

## How the tests catch bugs (so you know what "correct" means)

- **Reflection checks** (ex01, ex03) assert a field is declared `volatile`
  — this pins down the *mechanism*, not just an observed timing, since
  visibility bugs are famously nondeterministic (may pass by luck on your
  machine, fail in production under real JIT optimization).
- **High-iteration stress loops** (ex02, ex09) run hundreds of thousands of
  operations across many threads so races that would rarely surface in a
  quick manual test become essentially certain to surface.
- **Wall-clock assertions** (ex05, ex07, ex08) assert an operation
  completed faster than the sum of its parts would take sequentially —
  the only way to prove real parallelism happened, not just correctness.
- **Deadlock via bounded await** (ex06): worker threads are daemons
  submitted to a pool; the test calls `awaitTermination` with a timeout
  instead of joining forever, so a genuine deadlock fails the test cleanly
  in seconds instead of hanging the build.
- **Conservation/accounting checks** (ex04, ex06, ex09, ex10): every item
  pushed must be popped exactly once, every cent transferred must still
  exist somewhere, every permit acquired must be released — these catch
  lost updates and resource leaks that a simple "it ran without throwing"
  test would miss.

## Notes

- Targets **Java 25** (`maven.compiler.release=25`). Exercise 08 uses
  `Executors.newVirtualThreadPerTaskExecutor()` (virtual threads, finalized
  since Java 21) rather than the still-evolving structured concurrency
  preview API, so the project builds without `--enable-preview`.
- No exercise requires third-party libraries — everything is
  `java.util.concurrent` plus JUnit 5 for the test harness.
