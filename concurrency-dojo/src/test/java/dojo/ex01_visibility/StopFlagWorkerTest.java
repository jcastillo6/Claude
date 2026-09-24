package dojo.ex01_visibility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopFlagWorkerTest {

    @Test
    void runningFieldMustBeVolatile() throws NoSuchFieldException {
        Field f = StopFlagWorker.class.getDeclaredField("running");
        assertTrue(Modifier.isVolatile(f.getModifiers()),
                "The 'running' field must be volatile to guarantee visibility across threads");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void workerStopsPromptlyWhenFlagIsCleared() throws InterruptedException {
        StopFlagWorker worker = new StopFlagWorker();
        Thread t = new Thread(worker, "worker");
        t.setDaemon(true);
        t.start();

        // Let it spin long enough that the JIT has had a chance to
        // optimize the loop (and hoist a non-volatile read out of it).
        Thread.sleep(200);
        long before = worker.getIterations();
        assertTrue(before > 0, "worker should have made progress before stop()");

        worker.stop();
        t.join(3000);

        assertFalse(t.isAlive(),
                "worker thread should have terminated shortly after stop() was called "
                        + "— if it's still alive, the write to 'running' never became visible");
    }
}
