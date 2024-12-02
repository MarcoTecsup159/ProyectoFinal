package com.example.proyectofinal.ui

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectofinal.navigation.AppScreen
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.calculateRouteDistance
import com.example.proyectofinal.utils.RouteUtils.obtenerRutasMasEficientes
import com.example.proyectofinal.viewmodel.Route
import com.example.proyectofinal.viewmodel.encontrarPuntoMasCercano
import com.example.proyectofinal.viewmodel.fetchRoutePoints
import com.example.proyectofinal.viewmodel.getAddressFromLatLng
import com.example.proyectofinal.viewmodel.obtenerRutasDeFirebase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapScreen(
    originLatLng: LatLng,
    destinationLatLng: LatLng,
    empresaId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val rutasOptimas = remember { mutableStateOf<List<Route>>(emptyList()) }
    val rutaMasOptima = remember { mutableStateOf<Route?>(null) }
    val rutaSeleccionada = remember { mutableStateOf<Route?>(null) }
    val polyline = remember { mutableStateOf<List<LatLng>>(emptyList()) }
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

    LaunchedEffect(Unit) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )

        obtenerRutasDeFirebase { rutas ->
            val rutasEficientes = obtenerRutasMasEficientes(
                usuarioLatLng = originLatLng,
                destinoLatLng = destinationLatLng,
                rutas = rutas
            )
            rutasOptimas.value = rutasEficientes
            rutaMasOptima.value = rutasEficientes.firstOrNull()

            // Mover la cámara a la ruta óptima si está disponible
            rutaMasOptima.value?.let { ruta ->
                val puntos = ruta.routePoints
                if (puntos.isNotEmpty()) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(puntos.first().latitude, puntos.first().longitude),
                            15f
                        )
                    )
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
                    // Mapa para mostrar las rutas
                    Box(modifier = Modifier.weight(2f)) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            properties = mapProperties,
                            uiSettings = mapUiSettings,
                            cameraPositionState = cameraPositionState
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

                            // Dibuja la polilínea de la ruta seleccionada o más óptima
                            val rutaDibujar = rutaSeleccionada.value ?: rutaMasOptima.value
                            rutaDibujar?.let { ruta ->
                                Polyline(
                                    points = ruta.routePoints.map {
                                        LatLng(it.latitude, it.longitude)
                                    },
                                    color = Color.Blue,
                                    width = 8f
                                )
                            }

                            // Dibuja la polilínea de la guía si está disponible
                            if (polyline.value.isNotEmpty()) {
                                Polyline(
                                    points = polyline.value,
                                    color = Color.Blue, // Usa el color azul
                                    width = 8f,
                                    pattern = listOf(Dot(), Gap(20f)) // Dibuja en puntos
                                )
                            }
                        }
                    }

                    // Lista de rutas cercanas
                    val isExpanded = remember { mutableStateOf(true) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (isExpanded.value) 2f else 0.1f) // Ocupa un máximo de 1/3 de la pantalla
                    ) {
                        RutasExpandiblesList(
                            rutas = rutasOptimas.value,
                            isExpanded = isExpanded.value,
                            onExpandChange = {  isExpanded.value = !isExpanded.value },
                            polylineGuia = polyline,
                            userLocation = originLatLng,
                            onRutaClick = { ruta ->
                                rutaSeleccionada.value = ruta

                                // Mover la cámara a la ruta seleccionada
                                if (ruta.routePoints.isNotEmpty()) {
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                ruta.routePoints.first().latitude,
                                                ruta.routePoints.first().longitude
                                            ),
                                            15f
                                        )
                                    )
                                }
                            }
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

@Composable
fun RutasExpandiblesList(
    rutas: List<Route>,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    onRutaClick: (Route) -> Unit,
    polylineGuia: MutableState<List<LatLng>>,
    userLocation: LatLng
) {
    val isViewingDetails = remember { mutableStateOf(false) } // Estado para alternar entre lista y detalles
    val rutaSeleccionada = remember { mutableStateOf<Route?>(null) }

    Column(
        modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp, max = if (isExpanded) 500.dp else 56.dp)
    ) {
        IconButton(
            onClick = onExpandChange,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                contentDescription = if (isExpanded) "Ocultar rutas" else "Mostrar rutas"
            )
        }

        // Título del listado o detalle
        Text(
            text = if (isViewingDetails.value) "Detalle de la Ruta" else "Rutas encontradas",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp)
        )

        // Contenido principal
        if (isViewingDetails.value && rutaSeleccionada.value != null) {
            // Vista de detalles de la ruta seleccionada
            RutaDetalle(
                ruta = rutaSeleccionada.value!!,
                userLocation = userLocation,
                polylineGuia = polylineGuia,
                onBackToList = {
                    isViewingDetails.value = false // Regresar al listado
                }
            )
        } else if (isExpanded) {
            // Mostrar lista de rutas si `isExpanded` es verdadero
            if (rutas.isEmpty()) {
                // Mensaje si no hay rutas
                Text(
                    text = "No hay rutas disponibles.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(8.dp)
                ) {
                    items(rutas) { ruta ->
                        RutaItem(
                            ruta = ruta,
                            onClick = {
                                rutaSeleccionada.value = ruta // Establecer la ruta seleccionada
                                isViewingDetails.value = true // Cambiar a vista de detalles
                                onRutaClick(ruta) // Opcional, si necesitas manejar clics externos
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RutaItem(
    ruta: Route,
    onClick: () -> Unit,
) {

    val distanceKm = calculateRouteDistance(ruta.routePoints) / 1000 // Convertir a kilómetros
    val formattedDistance = String.format("%.2f", distanceKm) // Formatear con dos decimales

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = ruta.nombreRuta, style = MaterialTheme.typography.subtitle1)
            Text(
                text = "Distancia: $formattedDistance km", // Mostrar la distancia formateada
                style = MaterialTheme.typography.body2
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(android.graphics.Color.parseColor(ruta.empresaColor))) // Convierte el código hexadecimal a Color
            )
        }
    }
}


@Composable
fun RutaDetalle(
    ruta: Route,
    userLocation: LatLng,
    onBackToList: () -> Unit,
    polylineGuia: MutableState<List<LatLng>>
) {
    val context = LocalContext.current
    var Origen = getAddressFromLatLng(context, ruta.origen)
    var Destino = getAddressFromLatLng(context, ruta.destino)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Información principal de la ruta
            Text(text = ruta.nombreRuta, style = MaterialTheme.typography.h6)
            Text(
                text = "Origen: ${Origen}",
                style = MaterialTheme.typography.body2
            )
            Text(
                text = "Destino: ${Destino}",
                style = MaterialTheme.typography.body2
            )
            val distanceKm = calculateRouteDistance(ruta.routePoints) / 1000
            val formattedDistance = String.format("%.2f km", distanceKm)
            Text(text = "Distancia: $formattedDistance", style = MaterialTheme.typography.body2)

            // Franja de color de la empresa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(android.graphics.Color.parseColor(ruta.empresaColor)))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones para acciones
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onBackToList) {
                    Text("Volver al listado")
                }
                Button(
                    onClick = {
                        val puntoMasCercano = encontrarPuntoMasCercano(userLocation, ruta.routePoints)
                        puntoMasCercano?.let { destino ->
                            fetchRoutePoints(userLocation, destino, "AIzaSyAiaswLBAIKRY-IuRcX-JfRQ6VNBQnUGvw") { rutaGenerada ->
                                polylineGuia.value = rutaGenerada
                            }
                        }
                    }
                ) {
                    Text("Seguir ruta")
                }
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
