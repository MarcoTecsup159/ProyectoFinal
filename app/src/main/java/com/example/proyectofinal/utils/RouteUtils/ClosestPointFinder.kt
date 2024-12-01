package com.example.proyectofinal.utils.RouteUtils

import android.util.Log
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.distanceBetween
import com.example.proyectofinal.viewmodel.Route
import com.google.android.gms.maps.model.LatLng
import kotlin.math.sqrt

    fun findClosestPoints(route: Route, userLocation: LatLng, numPoints: Int = 3): List<LatLng> {
        if (route.routePoints.size < numPoints) {
            Log.e("findClosestPoints", "La ruta '${route.nombreRuta}' debe tener al menos $numPoints puntos para encontrar los más cercanos")
            return emptyList()
        }

        // Crear una lista de pares (distancia, punto) para ordenar
        val distances = route.routePoints.map { point ->
            Pair(distanceBetween(userLocation, point), point)
        }

        // Ordenar por distancia y tomar los primeros `numPoints`
        val closestPoints = distances.sortedBy { it.first }
            .take(numPoints)
            .map { it.second }

        Log.d("findClosestPoints", "Puntos cercanos a la ubicación del usuario ($userLocation) en la ruta '${route.nombreRuta}': $closestPoints")
        return closestPoints
    }

    fun findClosestPointOnBisector(
        userLocation: LatLng,
        closestPoints: List<LatLng>,
        routeName: String // Nombre de la ruta
    ): LatLng {
        if (closestPoints.size < 2) {
            Log.e(
                "findClosestPointOnBisector",
                "Ruta: $routeName. Se necesitan al menos dos puntos para calcular la bisectriz"
            )
            return userLocation
        }

        // Calcular el punto medio entre los puntos más cercanos
        val midLat = closestPoints.map { it.latitude }.average()
        val midLng = closestPoints.map { it.longitude }.average()
        Log.d("findClosestPointOnBisector", "Ruta: $routeName. Punto medio calculado: Lat=$midLat, Lng=$midLng")

        // Calcular la dirección desde el punto medio hasta la ubicación del usuario
        val directionLat = userLocation.latitude - midLat
        val directionLng = userLocation.longitude - midLng
        Log.d(
            "findClosestPointOnBisector",
            "Ruta: $routeName. Dirección hacia el usuario: Lat=$directionLat, Lng=$directionLng"
        )

        // Normalizar la dirección
        val length = sqrt(directionLat * directionLat + directionLng * directionLng)
        if (length == 0.0) {
            Log.e(
                "findClosestPointOnBisector",
                "Ruta: $routeName. El usuario está en el punto medio, no es necesario proyectar."
            )
            return LatLng(midLat, midLng)
        }
        val normalizedLat = directionLat / length
        val normalizedLng = directionLng / length
        Log.d(
            "findClosestPointOnBisector",
            "Ruta: $routeName. Vector normalizado: Lat=$normalizedLat, Lng=$normalizedLng"
        )

        // Proyectar el punto del usuario en la dirección de la bisectriz
        val projectionFactor = 0.5 // Ajustar según la distancia deseada para la proyección
        val projectedLat = midLat + normalizedLat * projectionFactor
        val projectedLng = midLng + normalizedLng * projectionFactor
        Log.d(
            "findClosestPointOnBisector",
            "Ruta: $routeName. Punto proyectado en la bisectriz: Lat=$projectedLat, Lng=$projectedLng"
        )

        return LatLng(projectedLat, projectedLng)
    }