package com.hisaab.hisaab;

import com.hisaab.hisaab.entity.Balance;
import com.hisaab.hisaab.repository.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private BalanceRepository balanceRepository;

    @Test
    public void testOptimisticLockingOnConcurrentBalanceUpdate() throws InterruptedException {

        // Use the actual ID of an existing balance row from your DB
        Long balanceId = 1L;

        // Sanity check: print current state before test
        Balance before = balanceRepository.findById(balanceId).orElseThrow();
        System.out.println("BEFORE TEST -> amount: " + before.getAmount() + ", version: " + before.getVersion());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        CountDownLatch readyLatch = new CountDownLatch(2);   // both threads loaded entity
        CountDownLatch startLatch = new CountDownLatch(1);   // signal to proceed with save
        CountDownLatch doneLatch = new CountDownLatch(2);    // both threads finished

        Runnable updateTask = () -> {
            try {
                // Step A: both threads read the SAME version of the row
                Balance balance = balanceRepository.findById(balanceId).orElseThrow();

                readyLatch.countDown();
                startLatch.await(); // wait until both have read, then proceed together

                // Step B: both modify and try to save
                balance.setAmount(balance.getAmount().add(BigDecimal.TEN));
                balanceRepository.save(balance); // version check happens here

                successCount.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + " -> SAVE SUCCESS");

            } catch (ObjectOptimisticLockingFailureException e) {
                failureCount.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + " -> SAVE FAILED (Optimistic Lock)");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };

        Thread t1 = new Thread(updateTask, "Thread-1");
        Thread t2 = new Thread(updateTask, "Thread-2");

        t1.start();
        t2.start();

        readyLatch.await();      // wait until both threads have read the row
        startLatch.countDown();  // release both to save at nearly the same time

        doneLatch.await();       // wait for both to finish

        System.out.println("Successes: " + successCount.get());
        System.out.println("Failures: " + failureCount.get());

        Balance after = balanceRepository.findById(balanceId).orElseThrow();
        System.out.println("AFTER TEST -> amount: " + after.getAmount() + ", version: " + after.getVersion());

        // Exactly one should succeed, one should fail
        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        // Version should have incremented by exactly 1 (only one successful write)
        assertEquals(before.getVersion() + 1, after.getVersion());
    }
}
