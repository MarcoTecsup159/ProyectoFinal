package com.example.proyectofinal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.proyectofinal.viewmodel.encontrarPuntoMasCercano
import com.example.proyectofinal.viewmodel.fetchRoutePoints
import com.example.proyectofinal.viewmodel.obtenerCoordenadas
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UserMapView(empresaId: String, rutaId: String, apiKey: String) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var coordenadas by remember { mutableStateOf(listOf<Pair<Double, Double>>()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var puntoMasCercano by remember { mutableStateOf<LatLng?>(null) }
    var routePolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            CoroutineScope(Dispatchers.Default).launch {
                locationResult.lastLocation?.let {
                    withContext(Dispatchers.Main) {
                        currentLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            }
        }
    }

    DisposableEffect(locationPermissionState.hasPermission, rutaId) {
        if (locationPermissionState.hasPermission) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Configura el LocationRequest para actualizaciones en tiempo real y alta precisión
                val locationRequest = LocationRequest.create().apply {
                    interval = 15000  // Intervalo de 10 segundos entre actualizaciones
                    fastestInterval = 10000  // Actualización mínima cada 5 segundos
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                // Inicia las actualizaciones de ubicación en tiempo real
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }

        // Obtén coordenadas adicionales para dibujar en el mapa
        obtenerCoordenadas(empresaId, rutaId) { coords, color ->
            coordenadas = coords
        }

        // Bloque de limpieza
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = currentLocation?.let {
            CameraPosition.fromLatLngZoom(it, 12f)
        } ?: CameraPosition.fromLatLngZoom(LatLng(-16.409047, -71.537451), 12f)
    }

    // Mostrar el mapa con las coordenadas cargadas
        Column(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState
            ) {
                currentLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Current Location"
                    )
                }

                val polylinePoints = coordenadas.map { LatLng(it.first, it.second) }
                if (polylinePoints.size > 1) {
                    Polyline(
                        points = polylinePoints,
                        color = Color.Red,
                        width = 10f
                    )
                }

                if (routePolyline.isNotEmpty()) {
                    Polyline(
                        points = routePolyline,
                        color = Color.Blue, // Usa el color azul
                        width = 8f,
                        pattern = listOf(Dot(), Gap(20f)) // Dibuja en puntos
                    )
                }

                puntoMasCercano?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Punto más cercano"
                    )
                }
            }
    }
}

