package com.example.proyectofinal.View

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
import android.content.pm.PackageManager
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.example.proyectofinal.BottomNavigationBar
import com.example.proyectofinal.DrawerContent
import com.example.proyectofinal.TopAppBar
import com.example.proyectofinal.viewmodels.obtenerCoordenadas
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.maps.android.compose.Polyline


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ModalNavigationDrawer que envuelve el contenido del Scaffold
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = { TopAppBar(navController, drawerState, scope) },  // Pasamos el drawerState y scope
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavigationHost(navController, Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = "map", modifier = modifier) {
        composable("map") { UserMapView("C4", "R1") }
        composable("createRoute") { RouteCreationMap() }
        composable("favoriteroute") { FavoriteRoute() }
        composable("profile") { ProfileScreen() }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UserMapView(empresaId: String, rutaId: String) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var coordenadas by remember { mutableStateOf(listOf<Pair<Double, Double>>()) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
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

    val cameraPositionState = rememberCameraPositionState {
        position = currentLocation?.let {
            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(it, 12f)
        } ?: com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(-16.409047, -71.537451), 12f)
    }

    // Mostrar el mapa con las coordenadas cargadas
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Mostrar la ubicación actual del usuario
        currentLocation?.let {
            Marker(
                state = rememberMarkerState(position = it),
                title = "Current Location"
            )
        }

        // Mostrar la ruta existente
        val polylinePoints = coordenadas.map { LatLng(it.first, it.second) }
        if (polylinePoints.size > 1) {
            Polyline(
                points = polylinePoints,
                color = Color.Red,
                width = 10f
            )
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
