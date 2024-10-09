package com.example.proyectofinal

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String?,  // Waypoints opcionales
        @Query("key") apiKey: String
    ): Call<DirectionsResponse>
}