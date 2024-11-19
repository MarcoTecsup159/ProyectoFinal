package com.example.proyectofinal

import com.google.android.gms.maps.model.LatLng

data class CalculatedRoute(
    val id: String,
    val points: List<LatLng>
)
