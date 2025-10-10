package aspectcapital.question2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class PriceHolderTest {

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    @Test
    @DisplayName("Given a null entity, when putPrice is called, then it throws NullPointerException")
    void givenNullEntityWhenPutPriceThenThrowsNPE() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.putPrice(null, bd("1.23")));
    }

    @Test
    @DisplayName("Given a null price, when putPrice is called, then it throws NullPointerException")
    void givenNullPriceWhenPutPriceThenThrowsNPE() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.putPrice("ENTITY", null));
    }

    @Test
    @DisplayName("Given entity doesn't exist in pricebook, when getPrice is called, then it throws NullPointerException")
    void givenUnknownEntity_whenGetPrice_thenThrowsNpe() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.getPrice("Testing123"));
    }

    @Test
    @DisplayName("Given entity doesn't exist in pricebook, when hasPriceChanged is called, then it throws NullPointerException")
    void givenUnknownEntity_whenHasPriceChanged_thenThrowsNpe() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When / Then
        assertThrows(NullPointerException.class, () -> priceHolder.hasPriceChanged("Testing123"));
    }

    @Test
    @DisplayName("Given the first put, when getPrice is called, then it returns the same price")
    void givenFirstPutWhenGetPriceThenReturnsSamePrice() {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        priceHolder.putPrice("StockA", bd("1.1000"));

        // When
        BigDecimal latest = priceHolder.getPrice("StockA");

        // Then
        assertEquals(bd("1.1000"), latest);
    }

    @Test
    @DisplayName("Given a new price added, when hasPriceChanged is checked before any get, then it reports a change")
    void givenFirstPutWhenHasPriceChangedBeforeGetThenTrue() {
        // Given
        PriceHolder priceHolder = new PriceHolder();

        // When
        priceHolder.putPrice("StockA", bd("1.2500"));
        boolean changed = priceHolder.hasPriceChanged("StockA");

        // Then
        assertTrue(changed);
    }

    @Test
    @DisplayName("Given a seen price, when hasPriceChanged is checked without new puts, then no change")
    void givenSeenPriceWhenHasPriceChangedThenFalse() {
        // Given
        PriceHolder priceHolder = new PriceHolder();
        priceHolder.putPrice("StockB", bd("722.00"));
        priceHolder.getPrice("StockB"); // mark as seen

        // When
        boolean changed = priceHolder.hasPriceChanged("StockB");

        // Then
        assertFalse(changed);
    }

    @Test
    @DisplayName("Given a seen price, when a different price is put, then hasPriceChanged returns true")
    void givenSeenPriceWhenNewPricePutThenHasPriceChangedTrue() {
        // Given
        PriceHolder holder = new PriceHolder();
        holder.putPrice("StockC", bd("323.00"));
        holder.getPrice("StockC"); // mark as seen

        // When
        holder.putPrice("StockC", bd("324.25"));
        boolean changed = holder.hasPriceChanged("StockC");

        // Then
        assertTrue(changed);
    }

    @Test
    @DisplayName("Given a seen price, when the same price with different scale is put, then should NOT count as change")
    void givenSeenPriceWhenSamePriceDifferentScaleThenNoChange() {
        // Given
        PriceHolder holder = new PriceHolder();
        holder.putPrice("StockD", bd("250"));
        holder.getPrice("StockD"); // mark as seen

        // When (same numeric value, different scale)
        holder.putPrice("StockD", bd("250.0"));
        boolean changed = holder.hasPriceChanged("StockD");

        // Then
        assertFalse(changed);
    }
}