package com.example.proyectofinal.ui

import android.content.pm.PackageManager
import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.proyectofinal.ui.components.PlaceAutocompleteTextField
import com.example.proyectofinal.viewmodel.getAddressFromLatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.proyectofinal.viewmodel.getCurrentLocationWithAddress

@Composable
fun SearchScreen(
    navController: NavHostController,
    onSearchComplete: (LatLng, LatLng) -> Unit
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
    var activeField by remember { mutableStateOf("") }

    // Lanzador para solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocationWithAddress(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onLocationRetrieved = { location, address ->
                    originLatLng = location
                    origin = address
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                },
                onError = { errorMessage ->
                    Log.e("LocationError", errorMessage)
                }
            )
        } else {
            // Manejar el caso donde el permiso no es otorgado
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Campo de texto para seleccionar origen con botón de ubicación actual
        Row(modifier = Modifier.fillMaxWidth()) {
            PlaceAutocompleteTextField(
                value = origin,
                onValueChange = { origin = it },
                label = "Seleccione destino",
                placesClient = placesClient,
                onPlaceSelected = { latLng, address ->
                    originLatLng = latLng
                    origin = address
                },
                getUserLocation = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocationWithAddress(
                            context = context,
                            fusedLocationClient = fusedLocationClient,
                            onLocationRetrieved = { location, address ->
                                originLatLng = location
                                origin = address
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                            },
                            onError = { errorMessage ->
                                Log.e("LocationError", errorMessage)
                            }
                        )
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                selectOnMap = {
                    activeField = "origin"
                }
        )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para seleccionar destino con botón de ubicación en mapa
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
                getUserLocation = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocationWithAddress(
                            context = context,
                            fusedLocationClient = fusedLocationClient,
                            onLocationRetrieved = { location, address ->
                                originLatLng = location
                                origin = address
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                            },
                            onError = { errorMessage ->
                                Log.e("LocationError", errorMessage)
                            }
                        )
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                selectOnMap = {
                    activeField = "destination"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (originLatLng != null && destinationLatLng != null) {
                    onSearchComplete(originLatLng!!, destinationLatLng!!)
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
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    if (activeField == "origin") {
                        originLatLng = latLng
                        origin = getAddressFromLatLng(context, latLng)
                    } else if (activeField == "destination") {
                        destinationLatLng = latLng
                        destination = getAddressFromLatLng(context, latLng)
                    }
                }            )
        }
    }
}