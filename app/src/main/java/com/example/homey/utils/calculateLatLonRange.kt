package com.example.homey.utils
import kotlin.math.*

fun calculateLatLonRange(latitude: Double, longitude: Double, radiusInKm: Double = 10.0): Pair<Pair<Double, Double>, Pair<Double, Double>> {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    // Calculate the latitude range
    val latRange = radiusInKm / earthRadius
    val minLatitude = latitude - Math.toDegrees(latRange)
    val maxLatitude = latitude + Math.toDegrees(latRange)

    // Calculate the longitude range
    val lonRange = radiusInKm / (earthRadius * cos(Math.toRadians(latitude)))
    val minLongitude = longitude - Math.toDegrees(lonRange)
    val maxLongitude = longitude + Math.toDegrees(lonRange)

    return Pair(Pair(minLatitude, maxLatitude), Pair(minLongitude, maxLongitude))
}