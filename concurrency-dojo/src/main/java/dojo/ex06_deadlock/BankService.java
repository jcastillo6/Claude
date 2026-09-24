package dojo.ex06_deadlock;

/**
 * Exercise 06 — Deadlock avoidance via lock ordering.
 *
 * A naive transfer() that does
 *   from.lock.lock(); try { to.lock.lock(); try { ... } finally { to.lock.unlock(); } }
 *   finally { from.lock.unlock(); }
 * deadlocks as soon as two threads transfer in opposite directions between
 * the same two accounts at the same time: thread A holds `from` and waits
 * for `to`, while thread B holds `to` (as its `from`) and waits for `from`.
 *
 * TODO: implement transfer() so that:
 *  - it never deadlocks, no matter how many threads call it concurrently,
 *    in any direction, between any accounts (hint: impose a total order
 *    on lock acquisition, e.g. by account id, independent of which
 *    account is the source and which is the destination).
 *  - it is atomic: no external observer holding neither lock ever sees a
 *    state where money has left one account but not yet arrived in the
 *    other.
 *  - it throws IllegalStateException (leaving both balances unchanged) if
 *    the source account has insufficient funds.
 */
public class BankService {

    public void transfer(Account from, Account to, long amountCents) {
        // TODO: implement a deadlock-free, atomic transfer.
        throw new UnsupportedOperationException("TODO: implement transfer()");
    }
}
