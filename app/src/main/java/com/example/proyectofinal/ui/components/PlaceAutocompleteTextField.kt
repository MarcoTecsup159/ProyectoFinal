package com.example.proyectofinal.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

@Composable
fun PlaceAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placesClient: PlacesClient,
    onPlaceSelected: (LatLng, String) -> Unit,
    modifier: Modifier = Modifier,
    getUserLocation: () -> Unit,
    selectOnMap: () -> Unit
) {
    val token = remember { AutocompleteSessionToken.newInstance() }
    val scope = rememberCoroutineScope()
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showButtons by remember { mutableStateOf(false) } // Estado para mostrar botones

    Column(modifier = modifier) {
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
                                suggestions = response.autocompletePredictions
                            }
                            .addOnFailureListener { exception ->
                                Log.e("PlaceError", "Error: $exception")
                                suggestions = emptyList()
                            }
                    } else {
                        suggestions = emptyList()
                    }
                }
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    // Muestra los botones cuando el TextField tiene el foco
                    showButtons = focusState.isFocused
                }
        )

        // Mostrar botones solo si `showButtons` es verdadero
        if (showButtons) {
            // Botón para obtener la ubicación actual
            Button(onClick = getUserLocation, modifier = Modifier.fillMaxWidth()) {
                Text("Usar ubicación actual")
            }

            // Botón para seleccionar en el mapa
            Button(onClick = selectOnMap, modifier = Modifier.fillMaxWidth()) {
                Text("Seleccionar en el mapa")
            }
        }

        // Lista de sugerencias debajo del TextField
        LazyColumn {
            items(suggestions) { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
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
                                        onValueChange(place.name ?: "")
                                    }
                                    suggestions = emptyList()
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
