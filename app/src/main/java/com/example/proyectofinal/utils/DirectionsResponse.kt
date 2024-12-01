package com.example.proyectofinal.utils

import com.example.proyectofinal.viewmodel.Route


data class DirectionsResponse(
    val routes: List<route>
)

data class route(
    val overview_polyline: OverviewPolyline?
)

data class OverviewPolyline(
    val points: String
)