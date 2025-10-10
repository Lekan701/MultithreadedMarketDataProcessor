package aspectcapital.question3;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class PriceHolder {

    private static class Details {
        final AtomicReference<BigDecimal> currentPrice = new AtomicReference<>();
        final AtomicReference<BigDecimal> hotPrice     = new AtomicReference<>();
    }

    private final ConcurrentHashMap<String, Details> priceBook = new ConcurrentHashMap<>();

    public void putPrice(String e, BigDecimal p) {
        if (Objects.isNull(e)) throw new NullPointerException("Entity is null");
        if (Objects.isNull(p)) throw new NullPointerException("Price is null");

        Details entityDetails = priceBook.computeIfAbsent(e, k -> new Details());

        synchronized (entityDetails) {
            BigDecimal oldPrice = entityDetails.hotPrice.get();

            boolean changed;
            if (oldPrice == null || oldPrice.compareTo(p) != 0) {
                changed = true;
            } else {
                changed = false;
            }

            entityDetails.hotPrice.set(p);
            if (changed) {
                entityDetails.notifyAll();
            }
        }
    }

    /** Reader: return latest and mark as seen */
    public BigDecimal getPrice(String e) {
        Details entityDetails = priceBook.get(e);
        if (entityDetails == null) return null;
        synchronized (entityDetails) {
            BigDecimal latest = entityDetails.hotPrice.get();
            entityDetails.currentPrice.set(latest);
            return latest;
        }
    }

    /** Has there been a change since last get/await? */
    public boolean hasPriceChanged(String e) {
        Details entityDetails = priceBook.get(e);
        if (entityDetails == null) return false;
        synchronized (entityDetails) {
            BigDecimal latestPrice = entityDetails.hotPrice.get();
            BigDecimal lastPrice = entityDetails.currentPrice.get();
            if (latestPrice == null && lastPrice == null) {
                return false;
            }
            if (latestPrice == null || lastPrice == null) {
                return true;
            }
            return latestPrice.compareTo(lastPrice) != 0;
        }
    }

    /**
     * Wait for the next price for e.
     * Return immediately if a new one already exists; otherwise block until it does.
     */
    public BigDecimal waitForNextPrice(String e) throws InterruptedException {
        if (e == null) throw new NullPointerException("Entity is null");
        Details entityDetails = priceBook.computeIfAbsent(e, k -> new Details());

        synchronized (entityDetails) {

            final BigDecimal myLastSeen = entityDetails.currentPrice.get();

            while (true) {
                BigDecimal latest = entityDetails.hotPrice.get();

                boolean changed;

                if (latest == null || myLastSeen == null) {
                    changed = (latest != myLastSeen);
                } else {
                    changed = (latest.compareTo(myLastSeen) != 0);
                }

                if (changed) {
                    entityDetails.currentPrice.set(latest);
                    return latest;
                }

                entityDetails.wait();
            }
        }
    }
}
