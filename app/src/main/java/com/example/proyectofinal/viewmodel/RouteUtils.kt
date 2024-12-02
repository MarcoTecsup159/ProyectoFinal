package com.example.proyectofinal.viewmodel

import android.location.Location
import com.google.android.gms.maps.model.LatLng


// Función para encontrar el punto más cercano
fun encontrarPuntoMasCercano(currentLocation: LatLng, coordenadas: List<LatLng>): LatLng? {
    var puntoMasCercano: LatLng? = null
    var distanciaMinima = Double.MAX_VALUE

    for (punto in coordenadas) {
        val distancia = calcularDistancia(currentLocation, punto)
        if (distancia < distanciaMinima) {
            distanciaMinima = distancia
            puntoMasCercano = punto
        }
    }

    return puntoMasCercano
}

// Función para calcular la distancia entre dos puntos
fun calcularDistancia(punto1: LatLng, punto2: LatLng): Double {
    val resultado = FloatArray(1)
    Location.distanceBetween(
        punto1.latitude, punto1.longitude,
        punto2.latitude, punto2.longitude,
        resultado
    )
    return resultado[0].toDouble() // Devuelve la distancia en metros
}

