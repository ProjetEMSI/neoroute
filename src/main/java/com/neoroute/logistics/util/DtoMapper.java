package com.neoroute.logistics.util;

import com.neoroute.logistics.domain.station.Station;
import com.neoroute.logistics.domain.trip.StationVisit;
import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.dto.station.StationResponse;
import com.neoroute.logistics.dto.trip.StationVisitResponse;
import com.neoroute.logistics.dto.trip.TripResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping entities to DTOs
 */
public final class DtoMapper {

    private DtoMapper() {
        // Prevent instantiation
    }

    public static StationResponse toStationResponse(Station station) {
        if (station == null)
            return null;

        return StationResponse.builder()
                .id(station.getId())
                .name(station.getName())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .active(station.isActive())
                .build();
    }

    public static List<StationResponse> toStationResponseList(List<Station> stations) {
        if (stations == null)
            return null;
        return stations.stream()
                .map(DtoMapper::toStationResponse)
                .collect(Collectors.toList());
    }

    public static StationVisitResponse toStationVisitResponse(StationVisit visit) {
        if (visit == null)
            return null;

        return StationVisitResponse.builder()
                .station(toStationResponse(visit.getStation()))
                .visitOrder(visit.getVisitOrder())
                .distanceFromPrevious(visit.getDistanceFromPrevious())
                .visited(visit.getVisited())
                .visitedAt(visit.getVisitedAt())
                .build();
    }

    public static List<StationVisitResponse> toStationVisitResponseList(List<StationVisit> visits) {
        if (visits == null)
            return null;
        return visits.stream()
                .map(DtoMapper::toStationVisitResponse)
                .collect(Collectors.toList());
    }

    public static TripResponse toTripResponse(Trip trip) {
        if (trip == null)
            return null;

        String timeFormatted = trip.getEstimatedTimeMinutes() != null
                ? TimeUtil.formatDuration(trip.getEstimatedTimeMinutes())
                : null;

        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .startStation(toStationResponse(trip.getStartStation()))
                .requiredStations(toStationResponseList(trip.getRequiredStations()))
                .optimizedRoute(toStationVisitResponseList(trip.getOptimizedRoute()))
                .assignedDriverId(trip.getAssignedDriverId())
                .assignedDriverName(trip.getAssignedDriverName())
                .status(trip.getStatus())
                .totalDistance(trip.getTotalDistance())
                .estimatedTimeMinutes(trip.getEstimatedTimeMinutes())
                .estimatedTimeFormatted(timeFormatted)
                .actualStartTime(trip.getActualStartTime())
                .actualEndTime(trip.getActualEndTime())
                .createdAt(trip.getCreatedAt())
                .build();
    }

    public static List<TripResponse> toTripResponseList(List<Trip> trips) {
        if (trips == null)
            return null;
        return trips.stream()
                .map(DtoMapper::toTripResponse)
                .collect(Collectors.toList());
    }

    public static com.neoroute.logistics.dto.user.UserResponse toUserResponse(
            com.neoroute.logistics.domain.user.User user) {
        if (user == null)
            return null;

        return com.neoroute.logistics.dto.user.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .totalTrips(user.getTotalTrips())
                .averageScore(user.getAverageScore())
                .build();
    }
}
