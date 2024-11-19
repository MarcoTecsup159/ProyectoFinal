package com.example.proyectofinal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.proyectofinal.Model.Empresa
import com.example.proyectofinal.Model.Ruta
import com.example.proyectofinal.viewmodels.obtenerEmpresas
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = { Text(text = "Mi Aplicación") },
        navigationIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        drawerState.open()  // Abre el drawer al hacer clic en el menú
                    }
                },
                modifier = Modifier.size(24.dp)  // Ajusta el tamaño del ícono del menú
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = { /* Acción de búsqueda */ }) {
                Icon(Icons.Filled.Search, contentDescription = "map")
            }
        }
    )
}



@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "map") },
            label = { Text("map") },
            selected = navController.currentDestination?.route == "map",
            onClick = { navController.navigate("maps") }
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
fun DrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    onRutaSelected: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val empresas = remember { mutableStateOf<List<Empresa>>(emptyList()) }

    // Llamar a la función que obtiene empresas en tiempo real
    LaunchedEffect(Unit) {
        obtenerEmpresasRealtime { empresasList ->
            empresas.value = empresasList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .background(Color.White)
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(text = "Menú de Navegación", style = MaterialTheme.typography.bodySmall)

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val buttonText = if (currentRoute == "createRoute") "Ver Rutas" else "Crear Ruta"
        Button(
            onClick = {
                scope.launch {
                    drawerState.close()
                    if (currentRoute == "createRoute") {
                        navController.navigate("map")
                    } else {
                        navController.navigate("createRoute")
                    }
                }
            },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(text = buttonText)
        }

        LazyColumn {
            items(empresas.value) { empresa ->
                EmpresaCard(empresa = empresa, onRutaClick = { empresaId, rutaId ->
                    onRutaSelected(empresaId, rutaId)
                })
            }
        }
    }
}

@Composable
fun EmpresaCard(empresa: Empresa, onRutaClick: (String, String) -> Unit) {
    var showRoutes by remember { mutableStateOf(false)}
    var rutas by remember { mutableStateOf<List<Ruta>>(emptyList()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                showRoutes = !showRoutes
                if (showRoutes) {
                    empresa.rutas?.let { rutasMap ->
                        rutas = rutasMap.values.toList()
                    }
                }
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Nombre de la empresa
            Text(text = empresa.nombre ?: "Sin nombre", style = MaterialTheme.typography.bodySmall)

            // Zonas de cobertura y unidad de negocio
            Text(text = "Zonas: ${empresa.zonaCobertura?.joinToString() ?: "Desconocidas"}")
            Text(text = "Unidad de Negocio: ${empresa.unidadNegocio ?: "No definida"}")

            Spacer(modifier = Modifier.height(8.dp))

            // Franja de color representando el color de la empresa
            empresa.color?.let { color ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(android.graphics.Color.parseColor(color)))
                )
            }

            // Mostrar las rutas si están disponibles
            if (showRoutes) {
                rutas.forEach { ruta ->
                    ruta.nombreRuta?.let { nombreRuta ->
                        RutaItem(ruta = ruta, onClick = {
                            // Depuración: Imprime el contenido del mapa de rutas y la ruta seleccionada
                            empresa.rutas?.let { rutasMap ->
                                println("Contenido de rutasMap: $rutasMap")
                                println("Ruta seleccionada: $ruta")

                                // Buscar el ID de la empresa y la ruta
                                val empresaId = empresa.unidadNegocio
                                val rutaId = rutasMap.entries.find { it.value == ruta }?.key

                                if (empresaId != null && rutaId != null) {
                                    // Depuración: Imprimir los IDs encontrados
                                    println("Empresa ID: $empresaId")
                                    println("Ruta ID: $rutaId")

                                    // Pasar los IDs de empresa y ruta
                                    onRutaClick(empresaId , rutaId)
                                } else {
                                    println("No se encontraron IDs correspondientes para la ruta seleccionada")
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun RutaItem(ruta: Ruta, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = ruta.nombreRuta ?: "Sin nombre")
            Text(text = "Origen: ${ruta.origen}")
            Text(text = "Destino: ${ruta.destino}")
        }
    }
}

fun obtenerEmpresasRealtime(onEmpresasChanged: (List<Empresa>) -> Unit) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("empresas")
    databaseRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val empresasList = mutableListOf<Empresa>()
            for (empresaSnapshot in snapshot.children) {
                val empresa = empresaSnapshot.getValue(Empresa::class.java)
                empresa?.let { empresasList.add(it) }
            }
            onEmpresasChanged(empresasList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Manejo de errores si es necesario
        }
    })
}

