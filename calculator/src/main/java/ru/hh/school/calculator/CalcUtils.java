package ru.hh.school.calculator;

import org.apache.commons.lang3.Validate;

import java.util.stream.StreamSupport;

/**
 * Created by ALinkov.<br/>
 * Date: 26.11.2018<br/>
 */
public final class CalcUtils {
    /**
     * Private constructor.
     */
    private CalcUtils() {
    }

    /**
     * @param numbers   input numbers
     * @return  sum of the numbers
     */
    public static double sum(final Iterable<Number> numbers) {
        Validate.notNull(numbers);
        return StreamSupport.stream(numbers.spliterator(), false)
                .mapToDouble(Number::doubleValue)
                .sum();
    }

    /**
     * @param numbers   input numbers
     * @return  average of the numbers
     */
    public static double average(final Iterable<Number> numbers) {
        Validate.notNull(numbers);
        return StreamSupport.stream(numbers.spliterator(), false)
                .mapToDouble(Number::doubleValue)
                .average().orElseThrow(() ->
                        new IllegalArgumentException("Incorrect arguments")
                );
    }
}
