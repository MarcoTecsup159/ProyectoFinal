package com.example.proyectofinal.utils

import com.google.android.gms.maps.model.LatLng

data class CalculatedRoute(
    val id: String,
    val points: List<LatLng>
)
