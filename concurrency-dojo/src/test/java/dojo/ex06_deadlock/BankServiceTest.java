package dojo.ex06_deadlock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankServiceTest {

    /**
     * Threads are daemons so that if the implementation under test
     * genuinely deadlocks, this test fails cleanly on the awaitTermination
     * timeout instead of hanging the whole build forever.
     */
    private static ExecutorService daemonPool(int size) {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        };
        return Executors.newFixedThreadPool(size, factory);
    }

    @Test
    @Timeout(value = 25, unit = TimeUnit.SECONDS)
    void concurrentOppositeDirectionTransfersNeverDeadlockAndConserveTotalBalance() throws InterruptedException {
        Account a = new Account(1, 1_000_000);
        Account b = new Account(2, 1_000_000);
        BankService bank = new BankService();

        final int threadsEachDirection = 8;
        final int transfersPerThread = 2_000;
        final long amount = 10;

        ExecutorService pool = daemonPool(threadsEachDirection * 2);
        AtomicInteger insufficientFundsCount = new AtomicInteger();

        for (int i = 0; i < threadsEachDirection; i++) {
            pool.submit(() -> runTransfers(bank, a, b, amount, transfersPerThread, insufficientFundsCount));
            pool.submit(() -> runTransfers(bank, b, a, amount, transfersPerThread, insufficientFundsCount));
        }

        pool.shutdown();
        boolean finished = pool.awaitTermination(20, TimeUnit.SECONDS);
        assertTrue(finished, "transfers did not complete in time — the implementation likely deadlocks");

        long total = a.getBalanceCents() + b.getBalanceCents();
        assertEquals(2_000_000, total, "total balance across accounts changed — a transfer was not atomic");
        assertTrue(a.getBalanceCents() >= 0 && b.getBalanceCents() >= 0, "a balance went negative");
    }

    private static void runTransfers(BankService bank, Account from, Account to, long amount,
                                      int count, AtomicInteger insufficientFundsCount) {
        for (int i = 0; i < count; i++) {
            try {
                bank.transfer(from, to, amount);
            } catch (IllegalStateException insufficientFunds) {
                insufficientFundsCount.incrementAndGet();
            }
        }
    }

    @Test
    void insufficientFundsLeavesBalancesUnchanged() {
        Account a = new Account(1, 50);
        Account b = new Account(2, 0);
        BankService bank = new BankService();

        assertThrows(IllegalStateException.class, () -> bank.transfer(a, b, 100));
        assertEquals(50, a.getBalanceCents());
        assertEquals(0, b.getBalanceCents());
    }

    @Test
    void successfulTransferMovesFunds() {
        Account a = new Account(1, 500);
        Account b = new Account(2, 100);
        BankService bank = new BankService();

        bank.transfer(a, b, 200);
        assertEquals(300, a.getBalanceCents());
        assertEquals(300, b.getBalanceCents());
    }
}
