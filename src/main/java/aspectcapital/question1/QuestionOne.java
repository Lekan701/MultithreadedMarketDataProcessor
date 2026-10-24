package aspectcapital.question1;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Question 1 implementation - Please add your map method here */
public class QuestionOne {

    public static <T, R> List<R> map(Function<T, R> function, List<T> list){

        if (function == null) {
            throw new NullPointerException("Function cant be null");
        }

        if (list == null) {
            throw new NullPointerException("List cant be null");
        }

        return list.stream().map(function).collect(Collectors.toList());
    }
}
