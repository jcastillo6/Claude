package dojo.ex06_deadlock;

import java.util.concurrent.locks.ReentrantLock;

public class Account {

    private final int id;
    private long balanceCents;
    final ReentrantLock lock = new ReentrantLock();

    public Account(int id, long balanceCents) {
        this.id = id;
        this.balanceCents = balanceCents;
    }

    public int getId() {
        return id;
    }

    long getBalanceCents() {
        return balanceCents;
    }

    void setBalanceCents(long balanceCents) {
        this.balanceCents = balanceCents;
    }
}
