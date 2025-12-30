package com.neoroute.logistics.util;

/**
 * Utility class for time calculations
 */
public final class TimeUtil {

    private TimeUtil() {
        // Prevent instantiation
    }

    /**
     * Calculate estimated time in minutes based on distance and speed
     * 
     * @param distanceKm Distance in kilometers
     * @param speedKmh   Speed in kilometers per hour
     * @return Estimated time in minutes
     */
    public static long calculateTimeInMinutes(double distanceKm, double speedKmh) {
        if (speedKmh <= 0) {
            throw new IllegalArgumentException("Speed must be positive");
        }
        double hours = distanceKm / speedKmh;
        return Math.round(hours * 60);
    }

    /**
     * Format minutes into human-readable duration
     * 
     * @param minutes Total minutes
     * @return Formatted string (e.g., "2h 30m")
     */
    public static String formatDuration(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, remainingMinutes);
        } else {
            return String.format("%dm", remainingMinutes);
        }
    }

    /**
     * Calculate time difference in minutes between two timestamps
     */
    public static long calculateDurationMinutes(long startTimeMillis, long endTimeMillis) {
        return (endTimeMillis - startTimeMillis) / (1000 * 60);
    }
}
