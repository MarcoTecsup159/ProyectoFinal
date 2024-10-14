package com.example.proyectofinal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController, drawerState: DrawerState, scope: CoroutineScope) {
    TopAppBar(
        title = { Text(text = "Mi Aplicación") },
        navigationIcon = {
            IconButton(
                onClick = {
                    // Abre el drawer cuando se presiona el icono de menú
                    scope.launch { drawerState.open() }
                }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = { /* Acción de búsqueda */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
    )}




        @Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Mapa") },
            label = { Text("Mapa") },
            selected = navController.currentDestination?.route == "map",
            onClick = { navController.navigate("map") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Rutas favoritas") },
            label = { Text("Favoritas") },
            selected = navController.currentDestination?.route == "favoriteroute",
            onClick = { navController.navigate("favoriteroute") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") }
        )
    }
}

@Composable
fun DrawerContent(navController: NavController, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "Menú de Navegación")

        // Un botón en el Drawer que cierra el drawer y navega a la pantalla de creación de rutas
        Button(
            onClick = {
                scope.launch {
                    drawerState.close()  // Cierra el Drawer
                    navController.navigate("createRoute")  // Navega a la pantalla de creación de rutas
                }
            },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(text = "Crear Ruta")
        }

        // Otros botones o elementos del menú se pueden agregar aquí
    }
}