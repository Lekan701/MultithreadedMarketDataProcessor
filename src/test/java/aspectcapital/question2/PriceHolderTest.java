
package aspectcapital.question2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class PriceHolderTest {

    private final PriceHolder priceHolder = new PriceHolder();

    @Test
    void shouldRejectNullEntityOnPutPrice() {
        assertThrows(NullPointerException.class,
                () -> priceHolder.putPrice(null, BigDecimal.TEN));
    }

    @Test
    void shouldRejectNullPriceOnPutPrice() {
        assertThrows(NullPointerException.class,
                () -> priceHolder.putPrice("A", null));
    }

    @Test
    void shouldRejectBothNullArguments() {
        assertThrows(NullPointerException.class,
                () -> priceHolder.putPrice(null, null));
    }

    @Test
    void shouldRejectEmptyEntityOnPutPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> priceHolder.putPrice("", BigDecimal.TEN));
    }

    @Test
    void shouldRejectNullEntityOnGetPrice() {
        assertThrows(NullPointerException.class,
                () -> priceHolder.getPrice(null));
    }

    @Test
    void shouldRejectEmptyEntityOnGetPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> priceHolder.getPrice(""));
    }

    @Test
    void shouldRejectNullEntityOnHasPriceChanged() {
        assertThrows(NullPointerException.class,
                () -> priceHolder.hasPriceChanged(null));
    }

    @Test
    void shouldRejectEmptyEntityOnHasPriceChanged() {
        assertThrows(IllegalArgumentException.class,
                () -> priceHolder.hasPriceChanged(""));
    }

    @Test
    void shouldReturnNullForUnknownEntity() {
        assertNull(priceHolder.getPrice("UNKNOWN"));
    }

    @Test
    void shouldReturnFalseForUnknownEntity() {
        assertFalse(priceHolder.hasPriceChanged("UNKNOWN"));
    }

    @Test
    void shouldRetrievePriceRegardlessOfSymbolCase() {

        priceHolder.putPrice("AAPL", BigDecimal.valueOf(100));

        assertEquals(
                BigDecimal.valueOf(100),
                priceHolder.getPrice("aapl")
        );

        assertEquals(
                BigDecimal.valueOf(100),
                priceHolder.getPrice("AaPl")
        );
    }

    @Test
    void shouldTrackUpdatesAcrossDifferentSymbolCases() {

        priceHolder.putPrice("AAPL", BigDecimal.valueOf(100));

        assertEquals(
                BigDecimal.valueOf(100),
                priceHolder.getPrice("aapl")
        );

        assertFalse(priceHolder.hasPriceChanged("AaPl"));

        priceHolder.putPrice("aapl", BigDecimal.valueOf(200));

        assertTrue(priceHolder.hasPriceChanged("AAPL"));

        assertEquals(
                BigDecimal.valueOf(200),
                priceHolder.getPrice("AaPl")
        );

        assertFalse(priceHolder.hasPriceChanged("aapl"));
    }

    @Test
    void shouldReturnLatestPrice() {
        priceHolder.putPrice("A", BigDecimal.TEN);
        priceHolder.putPrice("A", BigDecimal.valueOf(12));
        priceHolder.putPrice("A", BigDecimal.valueOf(15));

        assertEquals(BigDecimal.valueOf(15),
                priceHolder.getPrice("A"));
    }

    @Test
    void shouldResetChangedFlagAfterGetPrice() {
        priceHolder.putPrice("A", BigDecimal.TEN);

        assertTrue(priceHolder.hasPriceChanged("A"));

        assertEquals(BigDecimal.TEN, priceHolder.getPrice("A"));

        assertFalse(priceHolder.hasPriceChanged("A"));
    }

    @Test
    void shouldNotResetChangedFlagWhenCheckingChanged() {
        priceHolder.putPrice("A", BigDecimal.TEN);

        assertTrue(priceHolder.hasPriceChanged("A"));
        assertTrue(priceHolder.hasPriceChanged("A"));

        priceHolder.getPrice("A");

        assertFalse(priceHolder.hasPriceChanged("A"));
    }

    @Test
    void shouldTrackMultipleEntitiesIndependently() {
        priceHolder.putPrice("A", BigDecimal.TEN);
        priceHolder.putPrice("B", BigDecimal.valueOf(20));

        assertEquals(BigDecimal.TEN, priceHolder.getPrice("A"));

        assertFalse(priceHolder.hasPriceChanged("A"));
        assertTrue(priceHolder.hasPriceChanged("B"));

        assertEquals(BigDecimal.valueOf(20),
                priceHolder.getPrice("B"));

        assertFalse(priceHolder.hasPriceChanged("B"));
    }

    @Test
    void shouldMarkIdenticalPriceUpdateAsChanged() {
        priceHolder.putPrice("A", BigDecimal.TEN);
        priceHolder.getPrice("A");

        assertFalse(priceHolder.hasPriceChanged("A"));

        priceHolder.putPrice("A", BigDecimal.TEN);

        assertTrue(priceHolder.hasPriceChanged("A"));
    }

    @Test
    void shouldDetectUpdatesEvenWhenPriceReturnsToOriginal() {
        priceHolder.putPrice("A", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100),
                priceHolder.getPrice("A"));

        assertFalse(priceHolder.hasPriceChanged("A"));

        priceHolder.putPrice("A", BigDecimal.valueOf(150));
        priceHolder.putPrice("A", BigDecimal.valueOf(200));
        priceHolder.putPrice("A", BigDecimal.valueOf(100));

        assertTrue(priceHolder.hasPriceChanged("A"));

        assertEquals(BigDecimal.valueOf(100),
                priceHolder.getPrice("A"));

        assertFalse(priceHolder.hasPriceChanged("A"));
    }

    @Test
    void shouldMatchProvidedExample() {
        priceHolder.putPrice("a", BigDecimal.TEN);

        assertEquals(BigDecimal.TEN, priceHolder.getPrice("a"));

        priceHolder.putPrice("a", BigDecimal.valueOf(12));

        assertTrue(priceHolder.hasPriceChanged("a"));

        priceHolder.putPrice("b", BigDecimal.valueOf(2));
        priceHolder.putPrice("a", BigDecimal.valueOf(11));

        assertEquals(BigDecimal.valueOf(11),
                priceHolder.getPrice("a"));

        assertEquals(BigDecimal.valueOf(11),
                priceHolder.getPrice("a"));

        assertEquals(BigDecimal.valueOf(2),
                priceHolder.getPrice("b"));
    }

    @Test
    void shouldHandleConcurrentUpdatesToSameEntity()
            throws Exception {

        int threadCount = 10;
        int updateCount = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < updateCount; i++) {
                final int price = i;

                futures.add(executor.submit(() -> {
                    start.await();

                    priceHolder.putPrice(
                            "A", BigDecimal.valueOf(price));

                    return null;
                }));
            }

            start.countDown();

            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            BigDecimal latest = priceHolder.getPrice("A");

            assertNotNull(latest);
            assertTrue(latest.compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(latest.compareTo(
                    BigDecimal.valueOf(updateCount)) < 0);

            assertFalse(priceHolder.hasPriceChanged("A"));

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldHandleConcurrentReadsAndWrites()
            throws Exception {

        priceHolder.putPrice("A", BigDecimal.ZERO);

        ExecutorService executor =
                Executors.newFixedThreadPool(8);

        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++) {
                final int writerId = i;

                futures.add(executor.submit(() -> {
                    start.await();

                    for (int j = 0; j < 1000; j++) {
                        priceHolder.putPrice(
                                "A",
                                BigDecimal.valueOf(
                                        writerId * 1000L + j));
                    }

                    return null;
                }));
            }

            for (int i = 0; i < 4; i++) {
                futures.add(executor.submit(() -> {
                    start.await();

                    for (int j = 0; j < 1000; j++) {
                        BigDecimal price =
                                priceHolder.getPrice("A");

                        assertNotNull(price);
                        priceHolder.hasPriceChanged("A");
                    }

                    return null;
                }));
            }

            start.countDown();

            for (Future<?> future : futures) {
                future.get(15, TimeUnit.SECONDS);
            }

            assertNotNull(priceHolder.getPrice("A"));

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldHandleConcurrentUpdatesToDifferentEntities()
            throws Exception {

        int entityCount = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(entityCount);

        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < entityCount; i++) {
                final int id = i;

                futures.add(executor.submit(() -> {
                    start.await();

                    String entity = "ENTITY_" + id;

                    for (int j = 0; j < 100; j++) {
                        priceHolder.putPrice(
                                entity, BigDecimal.valueOf(j));
                    }

                    return null;
                }));
            }

            start.countDown();

            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            for (int i = 0; i < entityCount; i++) {
                String entity = "ENTITY_" + i;

                assertTrue(priceHolder.hasPriceChanged(entity));

                assertEquals(BigDecimal.valueOf(99),
                        priceHolder.getPrice(entity));

                assertFalse(priceHolder.hasPriceChanged(entity));
            }

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldNotLoseUpdateAfterCompletedRead()
            throws Exception {

        priceHolder.putPrice("A", BigDecimal.TEN);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch readCompleted = new CountDownLatch(1);

        try {
            Future<?> reader = executor.submit(() -> {
                assertEquals(BigDecimal.TEN,
                        priceHolder.getPrice("A"));

                readCompleted.countDown();
            });

            Future<?> writer = executor.submit(() -> {
                assertTrue(readCompleted.await(
                        5, TimeUnit.SECONDS));

                priceHolder.putPrice(
                        "A", BigDecimal.valueOf(20));

                return null;
            });

            reader.get(10, TimeUnit.SECONDS);
            writer.get(10, TimeUnit.SECONDS);

            assertTrue(priceHolder.hasPriceChanged("A"));

            assertEquals(BigDecimal.valueOf(20),
                    priceHolder.getPrice("A"));

            assertFalse(priceHolder.hasPriceChanged("A"));

        } finally {
            executor.shutdownNow();
        }
    }
}
