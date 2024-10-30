package com.example.proyectofinal.View

import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.proyectofinal.viewmodels.getCurrentLocation
@Composable
fun SearchScreen(
    navController: NavHostController,
    onSearchComplete: (LatLng, LatLng) -> Unit // Agregar este parámetro
) {
    val context = LocalContext.current
    var origin by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var originLatLng by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by rememberSaveable { mutableStateOf<LatLng?>(null) }
    val placesClient = remember { Places.createClient(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-16.409047, -71.537451), 12f)
    }

    // Lanzador para solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(fusedLocationClient) { location ->
                originLatLng = location
                origin = "Ubicación actual"
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f) // Nivel de zoom de 15 para centrarse en la ubicación
            }
        } else {
            // Manejar el caso donde el permiso no es otorgado
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()) {
        // Campo de texto para seleccionar origen con botón de ubicación actual
        Row(modifier = Modifier.fillMaxWidth()) {
            PlaceAutocompleteTextField(
                value = origin,
                onValueChange = { origin = it },
                label = "Seleccione origen",
                placesClient = placesClient,
                onPlaceSelected = { latLng, address ->
                    originLatLng = latLng
                    origin = address
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation(fusedLocationClient) { location ->
                        originLatLng = location
                        origin = "Ubicación actual"
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                    }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }) {
                Text("📍")
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para seleccionar destino con botón al lado
        Row(modifier = Modifier.fillMaxWidth()) {
            PlaceAutocompleteTextField(
                value = destination,
                onValueChange = { destination = it },
                label = "Seleccione destino",
                placesClient = placesClient,
                onPlaceSelected = { latLng, address ->
                    destinationLatLng = latLng
                    destination = address
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { /* Acción para obtener destino en el mapa */ }) {
                Text("📍")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (originLatLng != null && destinationLatLng != null) {
                    onSearchComplete(originLatLng!!, destinationLatLng!!)
                    // Navegar a MapScreen con las coordenadas
                    navController.navigate("map/${originLatLng!!.latitude}/${originLatLng!!.longitude}/${destinationLatLng!!.latitude}/${destinationLatLng!!.longitude}")
                }
            },
            enabled = originLatLng != null && destinationLatLng != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver en mapa")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vista del mapa
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            )
        }
    }
}

