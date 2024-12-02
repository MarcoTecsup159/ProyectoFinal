package com.example.proyectofinal.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
    } catch (e: IOException) {
        "Error getting address"
    }
}

fun getCurrentLocationWithAddress(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationRetrieved: (LatLng, String) -> Unit,
    onError: (String) -> Unit // Agrega un callback para manejar errores
) {
    // Asegúrate de verificar permisos antes de obtener la ubicación
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        onError("Permiso de ubicación no concedido")
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                CoroutineScope(Dispatchers.Main).launch {
                    val address = withContext(Dispatchers.IO) {
                        getAddressFromLatLng(context, latLng)
                    }
                    onLocationRetrieved(latLng, address)
                }
            } else {
                onError("No se encontró una ubicación anterior")
            }
        }
        .addOnFailureListener { exception ->
            onError("Error al obtener la ubicación: ${exception.message}")
        }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (LatLng) -> Unit) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            onLocationReceived(LatLng(it.latitude, it.longitude))
        }
    }
}