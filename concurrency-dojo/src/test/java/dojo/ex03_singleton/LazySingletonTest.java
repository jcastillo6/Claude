package dojo.ex03_singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LazySingletonTest {

    @Test
    void instanceFieldMustBeVolatile() throws NoSuchFieldException {
        Field f = LazySingleton.class.getDeclaredField("instance");
        assertTrue(Modifier.isVolatile(f.getModifiers()),
                "The 'instance' field must be volatile for double-checked locking to be correct");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void allThreadsSeeTheSameFullyConstructedInstance() throws Exception {
        final int threads = 32;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        Set<LazySingleton> observedInstances = new CopyOnWriteArraySet<>();
        Set<Integer> observedPayloadSizes = new CopyOnWriteArraySet<>();

        try {
            List<Future<Void>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Callable<Void> task = () -> {
                    startGate.await();
                    LazySingleton s = LazySingleton.getInstance();
                    observedInstances.add(s);
                    observedPayloadSizes.add(s.payloadSize());
                    return null;
                };
                futures.add(pool.submit(task));
            }

            startGate.countDown();
            for (Future<Void> f : futures) {
                f.get(15, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, observedInstances.size(), "getInstance() returned different references across threads");
        assertEquals(Set.of(10_000), observedPayloadSizes,
                "a thread observed a partially-constructed singleton (wrong payload size) — "
                        + "this is a classic double-checked-locking bug");
    }
}
