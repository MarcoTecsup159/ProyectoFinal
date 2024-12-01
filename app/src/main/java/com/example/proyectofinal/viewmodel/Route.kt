package com.example.proyectofinal.viewmodel

import com.google.android.gms.maps.model.LatLng

data class Route(
    val id: String,
    val routePoints: List<LatLng>, // Cambiado de 'points' a 'routePoints'
    val origen: LatLng,
    val destino: LatLng,
    val nombreRuta: String
)

data class DisplayRoute(
    val routeId: String,                // ID único de la ruta.
    val routeName: String,              // Nombre de la ruta.
    val companyName: String,            // Nombre de la empresa.
    val companyColor: String,           // Color de la empresa.
    val origin: LatLng,                 // Coordenadas del origen.
    val destination: LatLng,            // Coordenadas del destino.
    val intermediatePoints: List<LatLng> // Lista de puntos intermedios.
)
