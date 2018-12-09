package ru.hh.school.calculator;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by ALinkov<br/>
 * Date: 26.11.2018<br/>
 */
public class CalcUtilsTest {

    @Test(expected = NullPointerException.class)
    public void sumNull() {
        CalcUtils.sum(null);
    }

    @Test
    public void sum() {
        assertEquals(6., CalcUtils.sum(Arrays.asList(1, 2, 3)), 0.001);
    }

    @Test(expected = NullPointerException.class)
    public void averageNull() {
        CalcUtils.average(null);
    }

    @Test
    public void average() {
        assertEquals(2., CalcUtils.average(Arrays.asList(1, 2, 3)), 0.001);
    }
}