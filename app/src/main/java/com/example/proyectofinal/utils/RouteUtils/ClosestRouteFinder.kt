package com.example.proyectofinal.utils.RouteUtils

import android.util.Log
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.calculateDistance
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.distanceBetween
import com.example.proyectofinal.viewmodel.Route
import com.google.android.gms.maps.model.LatLng

fun obtenerRutasCercanas(
    usuarioLatLng: LatLng,
    rutas: List<Route>
): List<Route> {
    return rutas.filter { ruta ->
        if (ruta.routePoints.isNotEmpty()) {
            val distanciaAlInicio = calculateDistance(
                usuarioLatLng.latitude, usuarioLatLng.longitude,
                ruta.routePoints.first().latitude, ruta.routePoints.first().longitude
            )
            val distanciaAlFin = calculateDistance(
                usuarioLatLng.latitude, usuarioLatLng.longitude,
                ruta.routePoints.last().latitude, ruta.routePoints.last().longitude
            )
            distanciaAlInicio <= 10000 || distanciaAlFin <= 10000 // Ajustar el umbral según necesidades
        } else {
            false
        }
    }
}

fun obtenerRutasMasEficientes(
    usuarioLatLng: LatLng,
    destinoLatLng: LatLng,
    rutas: List<Route>,
    rangoProximidadOrigen: Double = 1000.0, // en metros
    rangoProximidadDestino: Double = 1000.0 // en metros
): List<Route> {
    val rutasCercanas = rutas.filter { ruta ->
        if (ruta.routePoints.isNotEmpty()) {
            val distanciaAlInicio = calculateDistance(
                usuarioLatLng.latitude, usuarioLatLng.longitude,
                ruta.routePoints.first().latitude, ruta.routePoints.first().longitude
            )
            val distanciaAlFin = calculateDistance(
                usuarioLatLng.latitude, usuarioLatLng.longitude,
                ruta.routePoints.last().latitude, ruta.routePoints.last().longitude
            )
            distanciaAlInicio <= 10000 || distanciaAlFin <= 10000 // Ajustar el umbral según necesidades
        } else {
            false
        }
    }

    return rutasCercanas.filter { ruta ->
        val puntosCercanosInicio = findClosestPoints(ruta, usuarioLatLng, numPoints = 2)
        val puntosCercanosDestino = findClosestPoints(ruta, destinoLatLng, numPoints = 2)

        if (puntosCercanosInicio.size >= 2 && puntosCercanosDestino.size >= 2) {
            val distanciaInicio = puntosCercanosInicio.minOf { point -> distanceBetween(usuarioLatLng, point) }
            val distanciaDestino = puntosCercanosDestino.minOf { point -> distanceBetween(destinoLatLng, point) }
            distanciaInicio <= rangoProximidadOrigen && distanciaDestino <= rangoProximidadDestino
        } else {
            false
        }
    }.sortedBy { ruta ->
        val puntosCercanosInicio = findClosestPoints(ruta, usuarioLatLng, numPoints = 2)
        val puntosCercanosDestino = findClosestPoints(ruta, destinoLatLng, numPoints = 2)

        val distanciaInicio = puntosCercanosInicio.minOf { point -> distanceBetween(usuarioLatLng, point) }
        val distanciaDestino = puntosCercanosDestino.minOf { point -> distanceBetween(destinoLatLng, point) }

        distanciaInicio + distanciaDestino
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