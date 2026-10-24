
package aspectcapital.question1;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class QuestionOneTest {

    @Test
    void shouldAddOneToEachInteger() {
        List<Integer> input = Arrays.asList(1, 2, 3);

        List<Integer> result =
                QuestionOne.map(n -> n + 1, input);

        assertEquals(Arrays.asList(2, 3, 4), result);
    }

    @Test
    void shouldMapStringsToIntegers() {
        List<String> input = Arrays.asList("Apple", "Kiwi");

        List<Integer> result =
                QuestionOne.map(String::length, input);

        assertEquals(Arrays.asList(5, 4), result);
    }

    @Test
    void shouldMapIntegersToStrings() {
        List<Integer> input = Arrays.asList(10, 20);

        List<String> result =
                QuestionOne.map(Object::toString, input);

        assertEquals(Arrays.asList("10", "20"), result);
    }

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        List<Integer> input = Collections.emptyList();

        List<Integer> result =
                QuestionOne.map(n -> n + 1, input);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertNotSame(input, result);
    }

    @Test
    void shouldRejectNullList() {
        assertThrows(NullPointerException.class,
                () -> QuestionOne.map(
                        String::length, null));
    }

    @Test
    void shouldRejectNullFunction() {
        List<Integer> input = Arrays.asList(1, 2);

        assertThrows(NullPointerException.class,
                () -> QuestionOne.map(null, input));
    }

    @Test
    void shouldNotModifyOriginalList() {
        List<Integer> input =
                new ArrayList<>(Arrays.asList(1, 2, 3));

        List<Integer> original = new ArrayList<>(input);

        List<Integer> result =
                QuestionOne.map(n -> n * 2, input);

        assertEquals(original, input);
        assertEquals(Arrays.asList(2, 4, 6), result);
        assertNotSame(input, result);
    }

    @Test
    void shouldPreserveOrderAndDuplicates() {
        List<Integer> input = Arrays.asList(3, 1, 3, 2);

        List<Integer> result =
                QuestionOne.map(n -> n * 2, input);

        assertEquals(Arrays.asList(6, 2, 6, 4), result);
    }

    @Test
    void shouldHandleNullElementsWithSafeFunction() {
        List<String> input = Arrays.asList("A", null, "B");

        List<Integer> result = QuestionOne.map(
                s -> s == null ? 0 : s.length(),
                input
        );

        assertEquals(Arrays.asList(1, 0, 1), result);
    }

    @Test
    void shouldAllowFunctionToReturnNull() {
        List<String> input = Arrays.asList("A", "B");

        List<String> result =
                QuestionOne.map(s -> null, input);

        assertEquals(Arrays.asList(null, null), result);
    }

    @Test
    void shouldPropagateFunctionException() {
        List<String> input = Arrays.asList("1", "invalid");

        assertThrows(NumberFormatException.class,
                () -> QuestionOne.map(
                        Integer::parseInt, input));
    }

    @Test
    void shouldNotMutateMutableElementsWithPureMapper() {
        StringBuilder first = new StringBuilder("hello");
        List<StringBuilder> input =
                Collections.singletonList(first);

        List<String> result = QuestionOne.map(
                sb -> sb.toString().toUpperCase(),
                input
        );

        assertEquals(Collections.singletonList("HELLO"), result);
        assertEquals("hello", first.toString());
        assertSame(first, input.get(0));
    }
}
