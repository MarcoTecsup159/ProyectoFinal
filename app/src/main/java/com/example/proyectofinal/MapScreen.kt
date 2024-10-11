package com.example.proyectofinal

import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.ui.theme.ProyectoFInalTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
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
import com.example.proyectofinal.View.FavoriteRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Polyline
import com.google.maps.android.ktx.model.polylineOptions
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Locale


@Composable
fun AppScreen() {
    val navController = rememberNavController()


    Scaffold(
        topBar = {TopBar()},
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavigationHost(navController, Modifier.padding(innerPadding)) // Pasar las rutas
    }
}
@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = "map", modifier = modifier) {
        composable("map") { MyGoogleMaps("C4","R1") }
        composable("favoriteroute") { FavoriteRoute() }
        composable("profile") { ProfileScreen() }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyGoogleMaps(empresaId: String, rutaId: String) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var routeName by remember { mutableStateOf("") }
    var coordenadas by remember { mutableStateOf(listOf<Pair<Double, Double>>()) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Efecto lanzado para obtener ubicación actual y coordenadas de la ruta
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
        // Llamada para obtener las coordenadas de la base de datos
        obtenerCoordenadas(empresaId, rutaId) { coords ->
            coordenadas = coords
        }
    }

    // Mapa con la posición de la cámara centrada en la ubicación actual
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

            // Dibujar los puntos de la ruta añadidos manualmente
            routePoints.forEachIndexed { index, point ->
                val markerState = rememberMarkerState(position = point)
                Circle(
                    center = point,
                    radius = 5.0,
                    strokeColor = Color.Blue,
                    strokeWidth = 1f,
                    fillColor = Color.Blue
                )
                LaunchedEffect(markerState.position) {
                    val newPoint = markerState.position
                    if (newPoint != point) {
                        routePoints = routePoints.toMutableList().apply {
                            set(index, newPoint)
                        }
                    }
                }
            }

            // Dibujar las líneas de la ruta manualmente
            if (routePoints.size > 1) {
                for (i in 0 until routePoints.size - 1) {
                    Polyline(
                        points = listOf(routePoints[i], routePoints[i + 1]),
                        color = Color.Blue,
                        width = 15f
                    )
                }
            }

            // Convertir las coordenadas a LatLng y dibujar Polyline en el mapa
            val polylinePoints = coordenadas.map { LatLng(it.first, it.second) }
            if (polylinePoints.size > 1) {
                Polyline(
                    points = polylinePoints,
                    color = Color.Red,
                    width = 10f
                )
            }
        }

        // Botones adicionales para controlar la vista y la ruta
        Button(
            onClick = {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            currentLocation = LatLng(it.latitude, it.longitude)
                            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                                currentLocation!!, 10f)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Center on Current Location")
        }

        // Botones para modificar la ruta
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

        // Botón para guardar la ruta
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Save Route")
        }
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Save Route") },
            text = {
                Column {
                    TextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        label = { Text("Route Name") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar los datos de la ruta
                    Text("Origin: ${routePoints.firstOrNull()?.let { getAddressFromLatLng(context, it) } ?: "N/A"}")
                    Text("Destination: ${routePoints.lastOrNull()?.let { getAddressFromLatLng(context, it) } ?: "N/A"}")

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Intermediate Points:")

                    // Mostrar los puntos intermedios
                    routePoints.forEachIndexed { index, point ->
                        Text("Point ${index + 1}: Lat ${point.latitude}, Lng ${point.longitude}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    saveRouteToFirebase(context, routeName, routePoints)
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}
fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
    } catch (e: IOException) {
        "Error getting address"
    }
}

fun saveRouteToFirebase(context: Context, routeName: String, routePoints: List<LatLng>) {
    val database = FirebaseDatabase.getInstance()
    val routesRef = database.getReference("empresas/C4/rutas")

    val geocoder = Geocoder(context, Locale.getDefault())
    var originAddress = ""
    var destinationAddress = ""

    try {
        val originAddresses = geocoder.getFromLocation(routePoints.first().latitude, routePoints.first().longitude, 1)
        originAddress = originAddresses?.firstOrNull()?.getAddressLine(0) ?: ""

        val destinationAddresses = geocoder.getFromLocation(routePoints.last().latitude, routePoints.last().longitude, 1)
        destinationAddress = destinationAddresses?.firstOrNull()?.getAddressLine(0) ?: ""
    } catch (e: IOException) {
        Log.e("Geocoder", "Error getting address", e)
    }

    // Verificación de campos vacíos
    if (originAddress.isEmpty()) originAddress = "Origen desconocido"
    if (destinationAddress.isEmpty()) destinationAddress = "Destino desconocido"
    if (routeName.isBlank()) {
        Toast.makeText(context, "El nombre de la ruta no puede estar vacío", Toast.LENGTH_SHORT).show()
        return
    }

    val routeData = mapOf(
        "nombreRuta" to routeName,
        "origen" to originAddress,
        "destino" to destinationAddress,
        "puntosIntermedio" to routePoints.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
    )

    // Obtén el número de rutas existentes para generar un identificador único
    routesRef.get().addOnSuccessListener { snapshot ->
        val routeCount = snapshot.childrenCount.toInt() + 1
        val routeId = "R$routeCount"  // Formato de identificador R#

        // Guarda la nueva ruta bajo el identificador generado
        routesRef.child(routeId).setValue(routeData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Ruta guardada con éxito bajo la clave: $routeId")
            } else {
                Log.e("Firebase", "Error guardando la ruta: ${task.exception?.message}")
            }
        }
    }.addOnFailureListener { e ->
        Log.e("Firebase", "Error obteniendo el conteo de rutas: ${e.message}")
    }
}


fun getDirections(context: Context, origin: LatLng, destination: LatLng, waypoints: List<LatLng>, callback: (List<LatLng>) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(DirectionsApiService::class.java)
    val waypointsStr = waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
    val call = service.getDirections("${origin.latitude},${origin.longitude}", "${destination.latitude},${destination.longitude}", waypointsStr, "AIzaSyDKZiHPz_IjoyrBkKj08G362TUyzni4vtw")

    call.enqueue(object : retrofit2.Callback<DirectionsResponse> {
        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
            if (response.isSuccessful) {
                val directionsResponse = response.body()
                if (directionsResponse != null && directionsResponse.routes.isNotEmpty()) {
                    val encodedPolyline = directionsResponse.routes[0].overviewPolyline.points
                    val decodedPath = PolyUtil.decode(encodedPolyline)
                    Log.d("Directions", "Decoded path: $decodedPath")
                    callback(decodedPath)
                } else {
                    Log.e("Directions", "No routes found")
                }
            } else {
                Log.e("Directions", "Response not successful: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            Log.e("Directions", "API call failed: ${t.message}")
        }
    })
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
