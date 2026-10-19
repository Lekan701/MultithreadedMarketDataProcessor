package aspectcapital.question1;
import java.util.*;
import java.util.function.Function;

/** Question 1 implementation - Please add your map method here */
public class QuestionOne {
  public static <T, F> List<F> map(Function<T, F> function, List<T> inputsList){

      List<F> results = new ArrayList<>();

      //test
      
      inputsList.forEach(input -> results.add(function.apply(input)));

      return results;
  }
}
