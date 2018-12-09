package ru.hh.school.calculator;

import org.apache.commons.lang3.Validate;

import java.util.stream.StreamSupport;

/**
 * Created by ALinkov<br/>
 * Date: 26.11.2018<br/>
 */
public class CalcUtils {
    private CalcUtils() {
    }

    public static double sum(Iterable<Number> numbers) {
        Validate.notNull(numbers);
        return StreamSupport.stream(numbers.spliterator(), false)
                .mapToDouble(Number::doubleValue)
                .sum();
    }

    public static double average(Iterable<Number> numbers) {
        Validate.notNull(numbers);
        return StreamSupport.stream(numbers.spliterator(), false)
                .mapToDouble(Number::doubleValue)
                .average().orElseThrow(() -> new IllegalArgumentException("Incorrect arguments"));
    }
}
