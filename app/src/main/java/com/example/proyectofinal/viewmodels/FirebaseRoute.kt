package com.example.proyectofinal.viewmodels

import com.google.android.gms.maps.model.LatLng

data class Route(
    val id: String,
    val routePoints: List<LatLng>, // Cambiado de 'points' a 'routePoints'
    val origen: LatLng,
    val destino: LatLng,
    val nombreRuta: String
)