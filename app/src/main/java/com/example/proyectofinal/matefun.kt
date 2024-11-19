package com.example.proyectofinal


import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*
import com.example.proyectofinal.viewmodels.Route

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

fun distanceBetween(pointA: LatLng, pointB: LatLng): Float {
    val locationA = Location("pointA").apply {
        latitude = pointA.latitude
        longitude = pointA.longitude
    }
    val locationB = Location("pointB").apply {
        latitude = pointB.latitude
        longitude = pointB.longitude
    }
    return locationA.distanceTo(locationB)
}

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

fun findClosestProjectionsOnRoute(
    routes: List<Route>,
    inicioUsuario: LatLng,
    destinoUsuario: LatLng
): Pair<Pair<String, LatLng>, Pair<String, LatLng>>? {

    // Iterar sobre las rutas para encontrar los puntos más cercanos al inicio del usuario
    val closestPointsToStart = routes.flatMap { route ->
        findClosestPoints(route, inicioUsuario, numPoints = 2).map { point -> route.nombreRuta to point }
    }

    if (closestPointsToStart.size < 2) {
        Log.e("findClosestProjections", "No se encontraron suficientes puntos cercanos al inicio en ninguna ruta")
        return null
    }
    Log.d("findClosestProjections", "Puntos más cercanos al inicio (${inicioUsuario}): $closestPointsToStart")

    // Proyectar el punto de inicio sobre la bisectriz de los puntos más cercanos
    val rutaInicio = closestPointsToStart.first().first // Nombre de la ruta para el inicio
    val puntoProyectadoInicio = findClosestPointOnBisector(
        inicioUsuario,
        closestPointsToStart.map { it.second },
        rutaInicio
    )
    Log.d("findClosestProjections", "Punto proyectado en la bisectriz al inicio: $puntoProyectadoInicio")

    // Iterar sobre las rutas para encontrar los puntos más cercanos al destino del usuario
    val closestPointsToEnd = routes.flatMap { route ->
        findClosestPoints(route, destinoUsuario, numPoints = 2).map { point -> route.nombreRuta to point }
    }

    if (closestPointsToEnd.size < 2) {
        Log.e("findClosestProjections", "No se encontraron suficientes puntos cercanos al destino en ninguna ruta")
        return null
    }
    Log.d("findClosestProjections", "Puntos más cercanos al destino (${destinoUsuario}): $closestPointsToEnd")

    // Proyectar el punto de destino sobre la bisectriz de los puntos más cercanos
    val rutaDestino = closestPointsToEnd.first().first // Nombre de la ruta para el destino
    val puntoProyectadoDestino = findClosestPointOnBisector(
        destinoUsuario,
        closestPointsToEnd.map { it.second },
        rutaDestino
    )
    Log.d("findClosestProjections", "Punto proyectado en la bisectriz al destino: $puntoProyectadoDestino")

    return Pair(
        rutaInicio to puntoProyectadoInicio,
        rutaDestino to puntoProyectadoDestino
    )
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


fun calculateRouteDistance(points: List<LatLng>): Double {
    var totalDistance = 0.0
    for (i in 0 until points.size - 1) {
        val start = points[i]
        val end = points[i + 1]
        totalDistance += calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
    }
    return totalDistance
}


// Implementación de Haversine
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371e3 // Radio de la Tierra en metros
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c // Distancia en metros
}


fun obtenerRutasCercanas(
    usuarioLatLng: LatLng,
    rutas: List<Route>
): List<Route> {
    return rutas.filter { ruta ->
        // Verifica que 'routePoints' no esté vacío antes de acceder a 'first()' y 'last()'
        if (ruta.routePoints.isNotEmpty()) {
            val distanciaAlInicio = calculateDistance(
                usuarioLatLng.latitude, usuarioLatLng.longitude,
                ruta.routePoints.first().latitude, ruta.routePoints.first().longitude
            )
            val distanciaAlFin = calculateDistance(
                usuarioLatLng.latitude, usuarioLatLng.longitude,
                ruta.routePoints.last().latitude, ruta.routePoints.last().longitude
            )
            Log.d("Distancias", "Distancia al inicio: $distanciaAlInicio, Distancia al fin: $distanciaAlFin")

            // Puedes ajustar el umbral de distancia según sea necesario
            distanciaAlInicio <= 10000 || distanciaAlFin <= 10000 // Distancia de 1 km
        } else {
            false
        }

    }
}

fun calcularRutaMasEficiente(
    usuarioLatLng: LatLng,
    destinoLatLng: LatLng,
    rutas: List<Route>,
    rangoProximidadOrigen: Double = 1000.0, // en metros
    rangoProximidadDestino: Double = 1000.0 // en metros
): Route? {
    // Almacenar la ruta más cercana y su distancia mínima
    var rutaMasCercana: Route? = null
    var menorDistanciaTotal: Double = Double.MAX_VALUE

    // Iterar sobre todas las rutas
    for (ruta in rutas) {
        // Encontrar los puntos más cercanos al inicio y destino
        val puntosCercanosInicio = findClosestPoints(ruta, usuarioLatLng, numPoints = 2)
        val puntosCercanosDestino = findClosestPoints(ruta, destinoLatLng, numPoints = 2)

        // Mostrar los puntos cercanos encontrados
        Log.d("CalcularRuta", "Puntos cercanos al inicio para la ruta ${ruta.nombreRuta}: $puntosCercanosInicio")
        Log.d("CalcularRuta", "Puntos cercanos al destino para la ruta ${ruta.nombreRuta}: $puntosCercanosDestino")

        // Verificar si hay suficientes puntos cercanos para evaluar
        if (puntosCercanosInicio.size >= 2 && puntosCercanosDestino.size >= 2) {
            // Calcular distancias al punto de inicio y al destino
            val distanciaInicio = puntosCercanosInicio.minOf { point -> distanceBetween(usuarioLatLng, point) }
            val distanciaDestino = puntosCercanosDestino.minOf { point -> distanceBetween(destinoLatLng, point) }

            Log.d("CalcularRuta", "Evaluando ruta: ${ruta.nombreRuta}")
            Log.d("CalcularRuta", "Distancia mínima al inicio: $distanciaInicio metros")
            Log.d("CalcularRuta", "Distancia mínima al destino: $distanciaDestino metros")

            // Si ambas distancias están dentro del rango, evaluar la ruta
            if (distanciaInicio <= rangoProximidadOrigen && distanciaDestino <= rangoProximidadDestino) {
                val distanciaTotal = distanciaInicio + distanciaDestino

                // Mostrar las rutas que pasan los filtros
                Log.d("CalcularRuta", "Ruta ${ruta.nombreRuta} pasa los filtros con distancia total: $distanciaTotal")

                // Actualizar la ruta más cercana si tiene menor distancia total
                if (distanciaTotal < menorDistanciaTotal) {
                    menorDistanciaTotal = distanciaTotal.toDouble()
                    rutaMasCercana = ruta
                }
            }
        } else {
            Log.w("CalcularRuta", "No se encontraron suficientes puntos cercanos para la ruta: ${ruta.nombreRuta}")
        }
    }

    if (rutaMasCercana != null) {
        Log.d("CalcularRuta", "Ruta más cercana encontrada: ${rutaMasCercana.nombreRuta}")
    } else {
        Log.d("CalcularRuta", "No se encontró una ruta cercana dentro de los rangos especificados.")
    }

    return rutaMasCercana
}
