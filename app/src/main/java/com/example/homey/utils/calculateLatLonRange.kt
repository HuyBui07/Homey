package com.example.homey.utils
import kotlin.math.*

fun calculateLatLonRange(latitude: Double, longitude: Double, radiusInKm: Double = 10.0): Pair<Pair<Double, Double>, Pair<Double, Double>> {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    // Calculate the latitude range
    val latRange = radiusInKm / earthRadius
    val minLatitude = max(8.179, latitude - Math.toDegrees(latRange))
    val maxLatitude = min(23.393, latitude + Math.toDegrees(latRange))

    // Calculate the longitude range
    val lonRange = radiusInKm / (earthRadius * cos(Math.toRadians(latitude)))
    val minLongitude = max(102.144, longitude - Math.toDegrees(lonRange))
    val maxLongitude = min(109.464, longitude + Math.toDegrees(lonRange))

    return Pair(Pair(minLatitude, maxLatitude), Pair(minLongitude, maxLongitude))
}