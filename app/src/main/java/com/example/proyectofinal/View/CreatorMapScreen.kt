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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.proyectofinal.viewmodels.saveRouteToFirebase
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.database.FirebaseDatabase
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
    var selectedEmpresa by remember { mutableStateOf("") }
    var empresas by remember { mutableStateOf<List<String>>(emptyList()) }  // Lista de empresas

    // Cargar las empresas desde Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val empresasRef = database.getReference("empresas")

        empresasRef.get().addOnSuccessListener { snapshot ->
            val empresaKeys = snapshot.children.map { it.key.orEmpty() }
            empresas = empresaKeys.filter { it.isNotBlank() }  // Filtrar claves no vacías
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

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
            currentLocation?.let {
                Marker(
                    state = rememberMarkerState(position = it),
                    title = "Current Location"
                )
            }

            routePoints.forEachIndexed { _, point ->
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

        Button(onClick = { showDialog = true }, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Save Route")
        }
    }

    if (showDialog) {
        SaveRouteDialog(
            routeName = routeName,
            onRouteNameChange = { routeName = it },
            routePoints = routePoints,
            onDismiss = { showDialog = false },
            onSave = { selectedEmpresa ->  // Empresa seleccionada pasada al guardar
                saveRouteToFirebase(context, selectedEmpresa, routeName, routePoints)
                showDialog = false
            },
            empresas = empresas,
            selectedEmpresa = selectedEmpresa,
            onEmpresaSelected = { selectedEmpresa = it }  // Actualizar empresa seleccionada
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SaveRouteDialog(
    routeName: String,
    onRouteNameChange: (String) -> Unit,
    routePoints: List<LatLng>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,  // Actualizado para pasar la empresa seleccionada
    empresas: List<String>,  // Lista de empresas desde Firebase
    selectedEmpresa: String,  // Empresa seleccionada actualmente
    onEmpresaSelected: (String) -> Unit  // Callback para seleccionar la empresa
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Save Route") },
        text = {
            Column {
                TextField(
                    value = routeName,
                    onValueChange = onRouteNameChange,
                    label = { Text("Route Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Agregar un Dropdown para seleccionar la empresa
                Text("Select Company:")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedEmpresa,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Company") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        empresas.forEach { empresa ->
                            DropdownMenuItem(onClick = {
                                onEmpresaSelected(empresa)  // Seleccionar empresa
                                expanded = false
                            }) {
                                Text(text = empresa)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Origin: ${routePoints.firstOrNull()?.latitude ?: "N/A"}, ${routePoints.firstOrNull()?.longitude ?: "N/A"}")
                Text("Destination: ${routePoints.lastOrNull()?.latitude ?: "N/A"}, ${routePoints.lastOrNull()?.longitude ?: "N/A"}")
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedEmpresa) }) {  // Pasar la empresa seleccionada
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
