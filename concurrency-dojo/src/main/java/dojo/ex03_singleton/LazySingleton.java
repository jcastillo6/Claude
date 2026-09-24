package dojo.ex03_singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Exercise 03 — Double-checked locking.
 *
 * The classic (broken) double-checked locking idiom:
 *
 * <pre>
 * if (instance == null) {
 *     synchronized (LazySingleton.class) {
 *         if (instance == null) {
 *             instance = new LazySingleton();
 *         }
 *     }
 * }
 * </pre>
 *
 * is unsafe unless {@code instance} is volatile. Without it, a thread can
 * observe a non-null reference to an object whose constructor has not
 * finished running from that thread's point of view (the reference write
 * can be reordered ahead of the writes that initialize the object's
 * fields).
 *
 * TODO: fix the field declaration and implement getInstance() correctly.
 */
public final class LazySingleton {

    // TODO: this field needs a modifier for double-checked locking to be
    // correct under the Java Memory Model.
    private static LazySingleton instance;

    private final List<String> payload;

    private LazySingleton() {
        // A deliberately "heavy" constructor to widen the window in which
        // a racing thread could observe a partially-constructed object.
        payload = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            payload.add("item-" + i);
        }
    }

    public static LazySingleton getInstance() {
        // TODO: implement correct double-checked locking here.
        throw new UnsupportedOperationException("TODO: implement getInstance()");
    }

    public int payloadSize() {
        return payload.size();
    }
}
