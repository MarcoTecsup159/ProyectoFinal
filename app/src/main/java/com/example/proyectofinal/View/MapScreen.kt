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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.proyectofinal.BottomNavigationBar
import com.example.proyectofinal.DrawerContent
import com.example.proyectofinal.TopAppBar
import com.example.proyectofinal.viewmodels.obtenerCoordenadas
import com.example.proyectofinal.viewmodels.updateRouteInFirebase
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedEmpresaId by remember { mutableStateOf<String?>(null) }
    var selectedRutaId by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, drawerState) { empresaId, rutaId ->
                selectedEmpresaId = empresaId
                selectedRutaId = rutaId
                scope.launch {
                    drawerState.close()
                    navController.navigate("map")
                }
            }
        },
        modifier = Modifier.fillMaxWidth(0.75f)  // El Drawer ocupará el 75% del ancho
    ) {
        Scaffold(
            topBar = { TopAppBar(navController, drawerState) },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavigationHost(navController, Modifier.padding(innerPadding), selectedEmpresaId, selectedRutaId)
        }
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    empresaId: String?,
    rutaId: String?
) {
    NavHost(navController, startDestination = "map", modifier = modifier) {
        composable("map") {
            if (empresaId != null && rutaId != null) {
                UserMapView(empresaId, rutaId)
            } else {
                Text("Seleccione una ruta desde el menú")
            }
        }
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
    var showEditDialog by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Efecto lanzado para obtener ubicación actual y coordenadas de la ruta
    LaunchedEffect(locationPermissionState.hasPermission, rutaId) {  // Añadimos rutaId aquí
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
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    ) {
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

    if (showEditDialog) {
        EditRouteDialog(
            empresaId = empresaId,
            rutaId = rutaId,
            onDismiss = { showEditDialog = false },
            onSave = { newEmpresaId, newRouteName ->
                // Actualizar los datos en Firebase
                updateRouteInFirebase(context, empresaId, rutaId, newEmpresaId, newRouteName)
                showEditDialog = false
            }
        )
    }
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
