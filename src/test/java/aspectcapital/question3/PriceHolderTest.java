package aspectcapital.question3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


class PriceHolderTest {


    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private static void sleepQuiet(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }


    @Test
    void givenNoPriceWhenGetPriceThenReturnNull() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When
        BigDecimal price = priceHolder.getPrice("Testing");

        // Then
        assertNull(price);
    }

    @Test
    void givenNoEntityWhenHasPriceChangedThenReturnFalse() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When
        boolean changed = priceHolder.hasPriceChanged("Testing1233");

        // Then
        assertFalse(changed, "Entity doesn't exist");
    }

    @Test
    void givenFirstPutWhenHasPriceChangedThenTrueIfGetPriceNotCalled() {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        priceHolder.putPrice("StockA", bd("1.10"));

        // When
        boolean changedBeforeGet = priceHolder.hasPriceChanged("StockA");
        BigDecimal read = priceHolder.getPrice("StockA");
        boolean changedAfterGet = priceHolder.hasPriceChanged("StockA");

        // Then
        assertTrue(changedBeforeGet, "First put is as a change");
        assertEquals(bd("1.10"), read);
        assertFalse(changedAfterGet, "After no change");
    }

    @Test
    void givenSeenPriceWhenSamePriceDifferentScaleThenNoChange() {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        priceHolder.putPrice("StockB", bd("12"));
        priceHolder.getPrice("StockB");

        // When
        priceHolder.putPrice("StockB", bd("12.0"));
        boolean changed = priceHolder.hasPriceChanged("StockB");

        // Then
        assertFalse(changed);
    }

    @Test
    void givenNewPriceAfterSeenWhenWaitForNextPriceThenReturnImmediately() throws Exception {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        priceHolder.putPrice("StockC", bd("190.00"));
        priceHolder.getPrice("StockC");

        priceHolder.putPrice("StockC", bd("191.25"));

        // When
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<BigDecimal> fut = pool.submit(() -> priceHolder.waitForNextPrice("StockC"));
        BigDecimal result = fut.get(300, TimeUnit.MILLISECONDS);
        pool.shutdownNow();

        // Then
        assertEquals(bd("191.25"), result, "Should return the new price immediately");
        assertFalse(priceHolder.hasPriceChanged("StockC"), "After returning, no further change pending");
    }

    @Test
    void givenWaitingThreadWhenFirstPriceArrivesThenWaiterReturnsThatPrice() throws Exception {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        String entity = "StockC";

        ExecutorService pool = Executors.newSingleThreadExecutor();
        AtomicReference<BigDecimal> observed = new AtomicReference<>();
        CountDownLatch waiting = new CountDownLatch(1);

        Future<?> waiter = pool.submit(() -> {
            try {
                waiting.countDown();
                BigDecimal next = priceHolder.waitForNextPrice(entity);
                observed.set(next);
            } catch (InterruptedException ignored) {}
        });

        // When
        sleepQuiet(120);
        priceHolder.putPrice(entity, bd("250.50"));

        waiter.get(1, TimeUnit.SECONDS);
        pool.shutdownNow();

        // Then
        assertEquals(bd("250.50"), observed.get(), "Waiter should return the first published price");
        assertFalse(priceHolder.hasPriceChanged(entity), "After waiter consumes, no change remains");
    }

    @Test
    void givenTwoWaitersWhenNextPriceArrivesThenBothReturnSamePrice() throws Exception {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        String entity = "StockC";

        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<BigDecimal> c = () -> priceHolder.waitForNextPrice(entity);

        Future<BigDecimal> f1 = pool.submit(c);
        Future<BigDecimal> f2 = pool.submit(c);

        // When
        sleepQuiet(100);
        priceHolder.putPrice(entity, bd("400.00"));

        BigDecimal r1 = f1.get(1, TimeUnit.SECONDS);
        BigDecimal r2 = f2.get(1, TimeUnit.SECONDS);
        pool.shutdownNow();

        // Then
        assertEquals(bd("400.00"), r1);
        assertEquals(bd("400.00"), r2);
        assertFalse(priceHolder.hasPriceChanged(entity));
    }

    @Test
    void givenNullEntityWhenPutPriceThenThrowsNPE() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.putPrice(null, bd("1.23")));
    }

    @Test
    void givenNullPriceWhenPutPriceThenThrowsNPE() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.putPrice("GBPUSD", null));
    }

    @Test
    void givenNullEntityWhenWaitForNextPriceThenThrowsNPE() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.waitForNextPrice(null));
    }

    @Test
    void givenNullEntityWhenGetPriceOrHasChangedThenConcurrentHashMapThrowsNPE() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.getPrice(null));
        assertThrows(NullPointerException.class, () -> priceHolder.hasPriceChanged(null));
    }

}