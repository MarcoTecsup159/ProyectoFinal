package com.example.proyectofinal.View

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyectofinal.ui.theme.ProyectoFInalTheme
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

@Composable
fun MapScreen(originLatLng: LatLng, destinationLatLng: LatLng) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(originLatLng, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberMarkerState(position = originLatLng),
            title = "Origen"
        )
        Marker(
            state = rememberMarkerState(position = destinationLatLng),
            title = "Destino"
        )
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun EditRouteDialog(
    empresaId: String,
    rutaId: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var newEmpresaId by remember { mutableStateOf(empresaId) }
    var newRouteName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Route") },
        text = {
            Column {
                TextField(
                    value = newEmpresaId,
                    onValueChange = { newEmpresaId = it },
                    label = { Text("Company ID") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = newRouteName,
                    onValueChange = { newRouteName = it },
                    label = { Text("Route Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newEmpresaId, newRouteName) }) {
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


@Composable
fun PlaceAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placesClient: PlacesClient,
    onPlaceSelected: (LatLng, String) -> Unit
) {
    val token = remember { AutocompleteSessionToken.newInstance() }
    val scope = rememberCoroutineScope()
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    Column {
        // TextField para entrada de usuario
        TextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input)
                scope.launch {
                    if (input.isNotEmpty()) {
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setSessionToken(token)
                            .setQuery(input)
                            .setCountries("PE") // Limitar a Perú
                            .setLocationBias(
                                RectangularBounds.newInstance(
                                    LatLng(-16.409047, -71.537451), // Coordenadas de Arequipa, Perú
                                    LatLng(-16.290154, -71.510780)
                                )
                            )
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                suggestions = response.autocompletePredictions // Actualiza las sugerencias
                            }
                            .addOnFailureListener { exception ->
                                Log.e("PlaceError", "Error: $exception")
                                suggestions = emptyList() // Vacía la lista en caso de error
                            }
                    } else {
                        suggestions = emptyList() // Limpia las sugerencias si el texto está vacío
                    }
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )

        // Lista de sugerencias debajo del TextField
        LazyColumn {
            items(suggestions) { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Obtén la ubicación seleccionada y llama a `onPlaceSelected` con dirección
                            val placeId = prediction.placeId
                            val placeRequest = FetchPlaceRequest
                                .builder(placeId, listOf(Place.Field.LAT_LNG, Place.Field.NAME))
                                .build()
                            placesClient
                                .fetchPlace(placeRequest)
                                .addOnSuccessListener { placeResponse ->
                                    val place = placeResponse.place
                                    place.latLng?.let { latLng ->
                                        onPlaceSelected(latLng, place.name ?: "")
                                        onValueChange(
                                            place.name ?: ""
                                        ) // Actualiza el campo de texto con el nombre del lugar
                                    }
                                    suggestions = emptyList() // Limpiar la lista tras seleccionar
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("PlaceError", "Error fetching place: $exception")
                                }
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}


//Funcion para el perfil
//pasara a su propio archivo cuando se realice la vista
@Composable
fun ProfileScreen() {
    // Aquí puedes implementar la vista de perfil
    Text(text = "Perfil")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProyectoFInalTheme {
        AppScreen()
    }
}
