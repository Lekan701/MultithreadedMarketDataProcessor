package aspectcapital.question2;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/** Question 2 implementation */
public final class PriceHolder {

    private static class PriceState {
        BigDecimal price;
        boolean changed;

        PriceState(BigDecimal price) {
            this.price = price;
            this.changed = true;
        }
    }

    private final ConcurrentHashMap<String, PriceState> priceBook;

    public PriceHolder() {
        this.priceBook = new ConcurrentHashMap<>();
    }

    /** Called when a price ‘p’ is received for an entity ‘e’ */
    public void putPrice(String e, BigDecimal p) {

        if(e == null){
            throw new NullPointerException("Null entity");
        }

        if(e.isEmpty()){
            throw new IllegalArgumentException("Blank entity");
        }

        if(p == null){
            throw new NullPointerException("Invalid price");
        }

        e = e.toUpperCase();

        priceBook.compute(e, (key, state) -> {

            if (state == null) {
                return new PriceState(p);
            }

            synchronized (state) {
                state.price = p;
                state.changed = true;
            }

            return state;
        });

    }

    /** Called to get the latest price for entity ‘e’ */
    public BigDecimal getPrice(String e) {

        if(e == null){
            throw new NullPointerException("Null entity");
        }

        if(e.isEmpty()){
            throw new IllegalArgumentException("Blank entity");
        }

        e = e.toUpperCase();

        PriceState state = priceBook.get(e);

        if (state == null){
            return null;
        }

        synchronized (state) {
            state.changed = false;
            return state.price;
        }
    }

    /**
     * Called to determine if the price for entity ‘e’ has
     * changed since the last call to getPrice(e).
     */
    public boolean hasPriceChanged(String e) {

        if(e == null){
            throw new NullPointerException("Null entity");
        }

        if(e.isEmpty()){
            throw new IllegalArgumentException("Blank entity");
        }

        e = e.toUpperCase();

        PriceState state = priceBook.get(e);

        if (state == null) {
            return false;
        }

        synchronized (state) {
            return state.changed;
        }
    }
}
