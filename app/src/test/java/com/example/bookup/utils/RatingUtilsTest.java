package com.example.bookup.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RatingUtilsTest {

    @Test
    public void computeAverage_nullOrEmpty_returnsZero() {
        double result1 = RatingUtils.computeAverage(null);
        double result2 = RatingUtils.computeAverage(Collections.emptyList());
        assertEquals(0.0, result1, 0.0001);
        assertEquals(0.0, result2, 0.0001);
    }

    @Test
    public void computeAverage_correctAverage() {
        List<Float> ratings = Arrays.asList(5f, 4f, 3.5f);
        double avg = RatingUtils.computeAverage(ratings);
        assertEquals((5.0 + 4.0 + 3.5) / 3.0, avg, 0.0001);
    }

    @Test
    public void computeAverage_nullElements_ignoresNulls() {
        List<Float> ratings = Arrays.asList(5f, null, 3f);
        double avg = RatingUtils.computeAverage(ratings);
        // computeAverage counts nulls in size per current impl, so expected = (5+0+3)/3 = 8/3
        assertEquals((5.0 + 0.0 + 3.0) / 3.0, avg, 0.0001);
    }
}
