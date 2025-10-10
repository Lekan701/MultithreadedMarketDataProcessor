package aspectcapital.question2;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** Question 2 implementation */
public final class PriceHolder {

    private static class Details {
        final AtomicReference<BigDecimal> currentPrice = new AtomicReference<>();
        final AtomicReference<BigDecimal> hotPrice     = new AtomicReference<>();

        Details(BigDecimal firstPrice) {
            this.hotPrice.set(firstPrice);
        }
    }

    private final ConcurrentHashMap<String, Details> priceBook = new ConcurrentHashMap<>();

    public PriceHolder() { }

    /** Called when a price ‘p’ is received for an entity ‘e’ */
    public void putPrice(String e, BigDecimal p) {
        if (Objects.isNull(e)) throw new NullPointerException("Entity is null");
        if (Objects.isNull(p)) throw new NullPointerException("Price is null");

        Details details = priceBook.computeIfAbsent(e, key -> new Details(p));
        details.hotPrice.set(p);
    }

    /** Called to get the latest price for entity ‘e’ */
    public BigDecimal getPrice(String e) {
        Details entityDetails = priceBook.get(e);
        BigDecimal latestPrice = entityDetails.hotPrice.get();
        entityDetails.currentPrice.set(latestPrice);
        return latestPrice;
    }

    /**
     * Called to determine if the price for entity ‘e’ has
     * changed since the last call to getPrice(e).
     */
    public boolean hasPriceChanged(String e) {
        Details entityDetails = priceBook.get(e);

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
