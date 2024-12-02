package com.example.proyectofinal.viewmodel

import android.graphics.Color
import android.util.Log
import com.example.proyectofinal.utils.DirectionsApiService
import com.example.proyectofinal.utils.DirectionsResponse
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.decodePolyline
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


    fun dibujarRutaEnMapa(puntosRuta: List<LatLng>, googleMap: GoogleMap) {
        val polylineOptions = PolylineOptions()
            .addAll(puntosRuta)
            .color(Color.BLUE)
            .width(10f)

        googleMap.addPolyline(polylineOptions)
    }

    fun fetchRoutePoints(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        callback: (List<LatLng>) -> Unit
    ) {
        val directionsApiService = retrofit.create(DirectionsApiService::class.java)
        val originStr = "${origin.latitude},${origin.longitude}"
        val destinationStr = "${destination.latitude},${destination.longitude}"

        val call = directionsApiService.getDirections(originStr, destinationStr, null, apiKey)
        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { directionsResponse ->
                        val route = directionsResponse.routes.firstOrNull()
                        val polyline = route?.overview_polyline?.points
                        if (!polyline.isNullOrEmpty()) {
                            val routePoints = decodePolyline(polyline)
                            callback(routePoints)
                        } else {
                            Log.e("DirectionsAPI", "Polyline is empty or null")
                            callback(emptyList())
                        }
                    }
                } else {
                    Log.e("DirectionsAPI", "Response unsuccessful: ${response.errorBody()?.string()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                t.printStackTrace()
                callback(emptyList())
            }
        })
    }

    fun addMarkerOnMap( location: LatLng, title: String) {
        MarkerOptions()
            .position(location)
            .title(title)
    }