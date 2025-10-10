package aspectcapital.question1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;



class QuestionOneTest {

    private static <T> List<T> list(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    @Test
    @DisplayName("Given some integers, when I map them to strings, then I get their string forms")
    void givenIntsWhenMapToStringsThenStringsList() {
        // Given
        List<Integer> numbers = list(1, 2, 3, 10);
        Function<Integer, String> toStringFunction = Object::toString;

        // When
        List<String> result = QuestionOne.map(toStringFunction, numbers);

        // Then
        assertEquals(list("1", "2", "3", "10"), result);
    }

    @Test
    @DisplayName("Given some words, when I map to lengths, then I get the length of the words")
    void givenStringsWhenMapToLengthThenListOfLengths() {
        // Given
        List<String> words = list("a", "bb", "ccc", "hello");
        Function<String, Integer> stringLengthFunction = String::length;

        // When
        List<Integer> lengths = QuestionOne.map(stringLengthFunction, words);

        // Then
        assertEquals(list(1, 2, 3, 5), lengths);
    }

    @Test
    @DisplayName("Given an empty list, when I map it, then I get an empty list back")
    void givenEmptyListWhenMappedThenReturnEmptyList() {
        // Given
        List<String> empty = Collections.emptyList();
        Function<String, Integer> stringLengthFunction = String::length;

        // When
        List<Integer> result = QuestionOne.map(stringLengthFunction, empty);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a list with nulls, when mapper handles nulls, then mapping should return -1")
    void givenNullsInListWhenMappedThenNullReturnsNegativeOne() {
        // Given
        List<String> inputs = list(null, "hi", "testing", null);
        Function<String, Integer> stringLengthFunction = s -> (s == null ? -1 : s.length());

        // When
        List<Integer> result = QuestionOne.map(stringLengthFunction, inputs);

        // Then
        assertEquals(list(-1, 2, 7, -1), result);
    }

    @Test
    @DisplayName("Null function returns NullPointerException")
    void givenNullFunctionWhenMapAttemptThenThrowsNPE() {
        // Given
        List<String> inputs = list("Testing123...");

        // When / Then
        assertThrows(NullPointerException.class, () -> QuestionOne.map(null, inputs));
    }

    @Test
    @DisplayName("Given a null input list, when I call map, then I should get a NullPointerException")
    void givenNullListWhenMapThenThrowsNPE() {
        // Given
        Function<String, Integer> stringLengthFunction = String::length;

        // When / Then
        assertThrows(NullPointerException.class, () -> QuestionOne.map(stringLengthFunction, null));
    }

    @Test
    @DisplayName("Given inputs in a certain order, list transformation as per funcxtion preserves order")
    void givenInputsWhenMapThenOrderPreserved() {
        // Given
        List<Integer> inputs = list(5, 1, 4, 2, 3);
        Function<Integer, Integer> squaredFunction = x -> x * x;

        // When
        List<Integer> squared = QuestionOne.map(squaredFunction, inputs);

        // Then
        assertEquals(list(25, 1, 16, 4, 9), squared);
    }
}