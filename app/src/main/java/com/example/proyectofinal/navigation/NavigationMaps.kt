package com.example.proyectofinal.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectofinal.ui.components.BottomNavigationBar
import com.example.proyectofinal.ui.components.DrawerContent
import com.example.proyectofinal.ui.components.TopAppBar
import com.example.proyectofinal.ui.FavoriteRoute
import com.example.proyectofinal.ui.MapScreen
import com.example.proyectofinal.ui.ProfileScreen
import com.example.proyectofinal.ui.RouteCreationMap
import com.example.proyectofinal.ui.SearchScreen
import com.example.proyectofinal.ui.UserMapView
import com.google.android.gms.maps.model.LatLng
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
        gesturesEnabled = false,
        modifier = Modifier.fillMaxWidth(0.75f)
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
    NavHost(
        navController = navController,
        startDestination = "map",
        modifier = modifier
    ) {
        composable("map") {
            if (empresaId != null && rutaId != null) {
                // Muestra UserMapView si empresaId y rutaId no son nulos
                UserMapView(empresaId, rutaId, "AIzaSyAiaswLBAIKRY-IuRcX-JfRQ6VNBQnUGvw")
            } else {
                // Si empresaId o rutaId son nulos, muestra SearchScreen
                SearchScreen(navController = navController) { origin, destination ->
                    navController.navigate("map/${origin.latitude}/${origin.longitude}/${destination.latitude}/${destination.longitude}")
                }
            }
        }
        composable(
            route = "map/{originLat}/{originLng}/{destinationLat}/{destinationLng}",
            arguments = listOf(
                navArgument("originLat") { type = NavType.StringType },
                navArgument("originLng") { type = NavType.StringType },
                navArgument("destinationLat") { type = NavType.StringType },
                navArgument("destinationLng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val originLat = backStackEntry.arguments?.getString("originLat")?.toDoubleOrNull() ?: 0.0
            val originLng = backStackEntry.arguments?.getString("originLng")?.toDoubleOrNull() ?: 0.0
            val destinationLat = backStackEntry.arguments?.getString("destinationLat")?.toDoubleOrNull() ?: 0.0
            val destinationLng = backStackEntry.arguments?.getString("destinationLng")?.toDoubleOrNull() ?: 0.0
            MapScreen(
                originLatLng = LatLng(originLat, originLng),
                destinationLatLng = LatLng(destinationLat, destinationLng),
                empresaId = "tu_empresa_id" // Asegúrate de pasar el ID de la empresa correcto
            )
        }
        composable("createRoute") { RouteCreationMap() }
        composable("favoriteroute") { FavoriteRoute() }
        composable("profile") { ProfileScreen() }
    }
}