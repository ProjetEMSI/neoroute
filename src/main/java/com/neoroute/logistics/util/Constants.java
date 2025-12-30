package com.neoroute.logistics.util;

/**
 * Application-wide constants
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // JWT Constants
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "Bearer";

    // Route Optimization
    public static final int DEFAULT_AVERAGE_SPEED_KMH = 40;
    public static final int MAX_STATIONS_PER_TRIP = 50;

    // Distance
    public static final double EARTH_RADIUS_KM = 6371.0;

    // Scoring Weights
    public static final double ON_TIME_WEIGHT = 0.4;
    public static final double EFFICIENCY_WEIGHT = 0.4;
    public static final double COMPLETION_WEIGHT = 0.2;

    // Score Thresholds
    public static final double EXCELLENT_SCORE = 90.0;
    public static final double GOOD_SCORE = 75.0;
    public static final double ACCEPTABLE_SCORE = 60.0;
}
