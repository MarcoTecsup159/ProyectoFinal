package com.example.proyectofinal

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(lat / 1E5, lng / 1E5)
        poly.add(p)
    }

    return poly
}

fun distanceBetween(point1: LatLng, point2: LatLng): Double {
    val earthRadius = 6371000.0 // Radio de la tierra en metros
    val dLat = Math.toRadians(point2.latitude - point1.latitude)
    val dLng = Math.toRadians(point2.longitude - point1.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(point1.latitude)) * cos(Math.toRadians(point2.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

fun findTwoClosestPoints(route: List<LatLng>, userLocation: LatLng): Pair<LatLng, LatLng> {
    var closestPoint1 = route[0]
    var closestPoint2 = route[1]
    var minDist1 = Double.MAX_VALUE
    var minDist2 = Double.MAX_VALUE

    for (point in route) {
        val dist = distanceBetween(userLocation, point)
        if (dist < minDist1) {
            minDist2 = minDist1
            closestPoint2 = closestPoint1
            minDist1 = dist
            closestPoint1 = point
        } else if (dist < minDist2) {
            minDist2 = dist
            closestPoint2 = point
        }
    }

    return Pair(closestPoint1, closestPoint2)
}

fun findClosestPointOnBisector(userLocation: LatLng, point1: LatLng, point2: LatLng): LatLng {
    val midLat = (point1.latitude + point2.latitude) / 2
    val midLng = (point1.longitude + point2.longitude) / 2
    val midPoint = LatLng(midLat, midLng)

    val directionLat = userLocation.latitude - midPoint.latitude
    val directionLng = userLocation.longitude - midPoint.longitude

    val projectedLat = midPoint.latitude + directionLat * 0.5
    val projectedLng = midPoint.longitude + directionLng * 0.5

    return LatLng(projectedLat, projectedLng)
}

fun getClosestRoutePoint(route: List<LatLng>, userLocation: LatLng): LatLng {
    val (point1, point2) = findTwoClosestPoints(route, userLocation)
    return findClosestPointOnBisector(userLocation, point1, point2)
}
