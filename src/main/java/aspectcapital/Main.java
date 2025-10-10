package aspectcapital;

import aspectcapital.question1.QuestionOne;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println(String.join("\n",
                "Welcome to the Aspect Capital test.",
                "This project is provided as an outline if you wish to use it.",
                "However please don't feel limited by it, you are free to implement in a language/project structure of choice.",
                "All tests are anonymised and marked manually by the members of the team you'll be joining.",
                "Best of luck!"));

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        List<String> letters = Arrays.asList("A", "B", "C", "D", "E");


        System.out.println(QuestionOne.map(x -> 1 + x, letters));


        runQuestion2Demo();
        runQuestion3Demo();
    }


    // Question 2 sim
    private static void runQuestion2Demo() {
        System.out.println("\n----- Question 2 demo -----");

        aspectcapital.question2.PriceHolder q2 = new aspectcapital.question2.PriceHolder();

        String e = "EURUSD";

        // Given: first price arrives
        q2.putPrice(e, bd("1.1000"));
        System.out.println("[Q2] putPrice " + e + " = 1.1000");

        // When: check hasPriceChanged before any get
        boolean changedBeforeGet = q2.hasPriceChanged(e);
        System.out.println("[Q2] hasPriceChanged before getPrice? " + changedBeforeGet);

        // When: read the latest and mark as seen
        BigDecimal firstRead = q2.getPrice(e);
        System.out.println("[Q2] getPrice returns " + firstRead);

        // When: check hasPriceChanged again
        boolean changedAfterGet = q2.hasPriceChanged(e);
        System.out.println("[Q2] hasPriceChanged after getPrice? " + changedAfterGet);

        // When: new price arrives
        q2.putPrice(e, bd("1.1055"));
        System.out.println("[Q2] putPrice " + e + " = 1.1055");

        // Then: should report change
        boolean changedAfterUpdate = q2.hasPriceChanged(e);
        System.out.println("[Q2] hasPriceChanged after new put? " + changedAfterUpdate);

        // Read again
        BigDecimal secondRead = q2.getPrice(e);
        System.out.println("[Q2] getPrice returns " + secondRead);
    }

    // Question 3 sim
    private static void runQuestion3Demo() throws Exception {
        System.out.println("\n----- Question 3 demo -----");

        aspectcapital.question3.PriceHolder q3 = new aspectcapital.question3.PriceHolder();

        final String entity = "AAPL";

        // Case 1: waiter blocks until first price arrives
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<BigDecimal> waiter1 = pool.submit(() -> {
            System.out.println("[Q3] waiter1: waiting for next price for " + entity + "...");
            try {
                BigDecimal next = q3.waitForNextPrice(entity);
                System.out.println("[Q3] waiter1: got price " + next);
                return next;
            } catch (InterruptedException ie) {
                System.out.println("[Q3] waiter1: interrupted");
                return null;
            }
        });

        // simulate a small delay, then publish the first price
        sleepQuiet(250);
        q3.putPrice(entity, bd("190.00"));
        System.out.println("[Q3] putPrice " + entity + " = 190.00");

        // join waiter1
        waiter1.get(2, TimeUnit.SECONDS);

        // Mark as seen explicitly (showing getPrice flow)
        BigDecimal seen = q3.getPrice(entity);
        System.out.println("[Q3] getPrice returns " + seen + " (marked as seen)");

        // Case 2: new price already available -> waitForNextPrice returns immediately
        q3.putPrice(entity, bd("191.25"));
        System.out.println("[Q3] putPrice " + entity + " = 191.25 (already in cache)");

        Future<BigDecimal> waiter2 = pool.submit(() -> {
            try {
                System.out.println("[Q3] waiter2: calling waitForNextPrice (should be instant)...");
                return q3.waitForNextPrice(entity);
            } catch (InterruptedException e) {
                return null;
            }
        });

        BigDecimal instant = waiter2.get(500, TimeUnit.MILLISECONDS);
        System.out.println("[Q3] waiter2: got price " + instant + " (returned without blocking)");

        // Case 3: two waiters; both wake up on the next put
        Future<BigDecimal> wA = pool.submit(() -> {
            System.out.println("[Q3] wA: waiting...");
            try { return q3.waitForNextPrice(entity); } catch (InterruptedException e) { return null; }
        });
        Future<BigDecimal> wB = pool.submit(() -> {
            System.out.println("[Q3] wB: waiting...");
            try { return q3.waitForNextPrice(entity); } catch (InterruptedException e) { return null; }
        });

        sleepQuiet(200);
        q3.putPrice(entity, bd("192.10"));
        System.out.println("[Q3] putPrice " + entity + " = 192.10 (should wake both waiters)");

        BigDecimal rA = wA.get(2, TimeUnit.SECONDS);
        BigDecimal rB = wB.get(2, TimeUnit.SECONDS);
        System.out.println("[Q3] wA returned: " + rA);
        System.out.println("[Q3] wB returned: " + rB);

        pool.shutdownNow();
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private static void sleepQuiet(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}
