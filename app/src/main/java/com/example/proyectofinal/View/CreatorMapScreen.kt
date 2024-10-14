package com.example.proyectofinal.View

import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Polyline

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RouteCreationMap() {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var routeName by remember { mutableStateOf("") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Efecto lanzado para obtener la ubicación actual
    LaunchedEffect(locationPermissionState.hasPermission) {
        if (locationPermissionState.hasPermission) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = currentLocation?.let {
            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(it, 12f)
        } ?: com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(-16.409047, -71.537451), 12f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                routePoints = routePoints + latLng
            }
        ) {
            // Mostrar la ubicación actual con un marcador
            currentLocation?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    title = "Current Location"
                )
            }

            // Dibujar los puntos y líneas de la ruta
            routePoints.forEachIndexed { index, point ->
                val markerState = rememberMarkerState(position = point)
                Circle(
                    center = point,
                    radius = 5.0,
                    strokeColor = Color.Blue,
                    strokeWidth = 1f,
                    fillColor = Color.Blue
                )
            }

            if (routePoints.size > 1) {
                Polyline(
                    points = routePoints,
                    color = Color.Blue,
                    width = 15f
                )
            }
        }

        // Botones de control para la creación de rutas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                if (routePoints.isNotEmpty()) {
                    routePoints = routePoints.dropLast(1)
                }
            }) {
                Text("Remove Last Point")
            }
            Button(onClick = {
                routePoints = emptyList()
            }) {
                Text("Clear Route")
            }
        }

        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Save Route")
        }
    }

    if (showDialog) {
        SaveRouteDialog(routeName, routePoints, onDismiss = { showDialog = false })
    }
}

@Composable
fun SaveRouteDialog(routeName: String, routePoints: List<LatLng>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Save Route") },
        text = {
            Column {
                TextField(
                    value = routeName,
                    onValueChange = { /* Lógica para actualizar nombre */ },
                    label = { Text("Route Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar los puntos de la ruta
                Text("Origin: ${routePoints.firstOrNull()?.latitude ?: "N/A"}, ${routePoints.firstOrNull()?.longitude ?: "N/A"}")
                Text("Destination: ${routePoints.lastOrNull()?.latitude ?: "N/A"}, ${routePoints.lastOrNull()?.longitude ?: "N/A"}")
            }
        },
        confirmButton = {
            Button(onClick = {
                // Lógica para guardar ruta
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
