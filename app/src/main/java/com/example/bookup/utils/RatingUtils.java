package com.example.bookup.utils;

import java.util.List;

public final class RatingUtils {

    private RatingUtils() {}

    /**
     * Compute average of ratings (float values). Returns 0 if list is empty or null.
     */
    public static double computeAverage(List<Float> ratings) {
        if (ratings == null || ratings.isEmpty()) return 0.0;
        double total = 0.0;
        for (Float r : ratings) {
            if (r != null) total += r;
        }
        return total / ratings.size();
    }
}
