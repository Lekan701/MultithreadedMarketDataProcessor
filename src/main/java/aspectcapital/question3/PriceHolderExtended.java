package aspectcapital.question3;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/** Question 3 implementation */
public final class PriceHolderExtended {

    private static class PriceState {
        BigDecimal price;
        boolean changed;

        PriceState() {
            this.price = null;
            this.changed = false;
        }
    }

    private final ConcurrentHashMap<String, PriceState> priceBook;

    public PriceHolderExtended() {
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
                state = new PriceState();
            }

            synchronized (state) {
                state.price = p;
                state.changed = true;
                state.notifyAll();
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

    public BigDecimal waitForNextPrice(String e) throws InterruptedException{

        if (e == null) {
            throw new NullPointerException("Null entity");
        }

        if (e.isEmpty()) {
            throw new IllegalArgumentException("Blank entity");
        }

        e = e.toUpperCase();

        PriceState state = priceBook.computeIfAbsent(
                e, key -> new PriceState()
        );

        synchronized (state) {

            while (!state.changed) {
                state.wait();
            }

            state.changed = false;
            return state.price;
        }
    }
}
