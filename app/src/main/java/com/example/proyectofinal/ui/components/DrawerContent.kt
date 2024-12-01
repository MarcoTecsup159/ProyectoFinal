package com.example.proyectofinal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectofinal.model.empresa
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    onRutaSelected: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val empresas = remember { mutableStateOf<List<empresa>>(emptyList()) }

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