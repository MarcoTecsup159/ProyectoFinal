package com.example.proyectofinal.View

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyectofinal.ui.theme.ProyectoFInalTheme
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectofinal.calcularRutaMasEficiente
import com.example.proyectofinal.calculateRouteDistance
import com.example.proyectofinal.viewmodels.Route
import com.example.proyectofinal.viewmodels.obtenerRutasDeFirebase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    originLatLng: LatLng,
    destinationLatLng: LatLng,
    empresaId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val rutasOptimas = remember { mutableStateOf<List<Route>>(emptyList()) }
    val rutaMasOptima = remember { mutableStateOf<Route?>(null) }
    var mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }
    var mapUiSettings by remember { mutableStateOf(MapUiSettings()) }
    val context = LocalContext.current
    val locationPermissionGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Define esta constante para la solicitud de permisos
    val LOCATION_PERMISSION_REQUEST_CODE = 1000


    // Estado de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(originLatLng, 15f) // Inicializa con la posición de origen
    }

// En tu MapScreen
    LaunchedEffect(Unit) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        // Suponiendo que tienes la lista de rutas desde Firebase ya cargada en `rutas`
        obtenerRutasDeFirebase { rutas ->
            val rutaOptima = calcularRutaMasEficiente(
                usuarioLatLng = originLatLng,  // Posición inicial o del usuario
                destinoLatLng = destinationLatLng,  // Posición final o destino
                rutas = rutas.filter { it.routePoints.isNotEmpty() } // Filtramos las rutas vacías
            )

            // Actualizamos el estado con la ruta óptima
            rutaMasOptima.value = rutaOptima

            // Mover la cámara a la ruta óptima si está disponible
            rutaOptima?.let { ruta ->
                val puntos = ruta.routePoints
                if (puntos.isNotEmpty()) {
                    // Mueve la cámara al primer punto de la ruta
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(puntos.first().latitude, puntos.first().longitude), 15f))
                }
            }
        }
    }
    if (locationPermissionGranted.value) {

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ruta Óptima") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Mapa para mostrar la ruta más óptima
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        properties = mapProperties,
                        uiSettings = mapUiSettings,
                        cameraPositionState = cameraPositionState // Pasar el estado de la cámara
                    ) {
                        // Muestra los marcadores de origen y destino
                        Marker(
                            state = rememberMarkerState(position = originLatLng),
                            title = "Origen"
                        )
                        Marker(
                            state = rememberMarkerState(position = destinationLatLng),
                            title = "Destino"
                        )

                        // Dibuja la polilínea de la ruta más óptima si está disponible
                        rutaMasOptima.value?.let { ruta ->
                            val puntos = ruta.routePoints
                            if (puntos.isNotEmpty()) {
                                // Dibuja la polilínea en el mapa
                                Polyline(
                                    points = puntos.map { LatLng(it.latitude, it.longitude) },
                                    color = Color.Blue,
                                    width = 8f
                                )
                            } else {
                                Log.e("MapScreen", "Error: La ruta más óptima no tiene puntos para mostrar.")
                            }
                        }
                    }
                }

                // Mostrar información de la ruta más óptima
                rutaMasOptima.value?.let { ruta ->
                    Text(
                        text = "Ruta más óptima: ${ruta.id}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Distancia: ${calculateRouteDistance(ruta.routePoints)} km",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    )
    } else {
        // Mensaje indicando que se requiere permiso de ubicación
        Text("Se requiere permiso de ubicación para mostrar el mapa")
    }
}

// Composable para lista expandible de rutas
@Composable
fun RutasExpandiblesList(
    rutas: List<Route>,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    onRutaClick: (Route) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = onExpandChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isExpanded) "Ocultar rutas" else "Mostrar rutas adicionales")
        }
        if (isExpanded) {
            LazyColumn {
                items(rutas) { ruta ->
                    RutaItem(ruta = ruta, onClick = { onRutaClick(ruta) })
                }
            }
        }
    }
}

// Composable para cada ruta en la lista
@Composable
fun RutaItem(ruta: Route, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ruta: ${ruta.id}")
            Text("Distancia: ${calculateRouteDistance(ruta.routePoints)} km")
        }
    }
}

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
