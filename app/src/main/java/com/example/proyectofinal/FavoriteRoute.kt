package com.example.proyectofinal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.ui.theme.ProyectoFInalTheme

@Composable
fun FavoriteRoute() {
    val navController = rememberNavController()
    Scaffold(
        content = ColumnFav(),
    )
}

@Composable
fun ColumnFav(): @Composable (PaddingValues) -> Unit {
    return { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Texto de saludo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hola, Usuario 👋",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Imagen del perfil
                    Icon(
                        imageVector = Icons.Default.Person,  // Usa un ícono de Material Design en lugar de un recurso
                        contentDescription = "Profile",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de búsqueda
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Buscar ruta") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        Icon(Icons.Default.List, contentDescription = "Filtrar")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))


                // Lista de rutas favoritas
                FavoriteRouteList()
            }
            FloatingActionButton(
                onClick = { /* Navegar o abrir formulario de agregar nueva ruta */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Agregar ruta")
            }
        }
    }
}

@Composable
fun FilterButton(text: String, selected: Boolean = false) {
    Button(
        onClick = { /* Acción del botón */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun FavoriteRouteList() {
    val routes = listOf(
        "Cerro Colorado" to "Av. Perú",
        "Cayma" to "Av. Perú",
        "Paucarpata" to "Av. Perú",
        "Hunter" to "Av. Perú",
        "Socabaya" to "Tokyo, Japan"
    )

    LazyColumn {
        items(routes) { route ->
            FavoriteRouteItem(route.first, route.second)
        }
    }
}

@Composable
fun FavoriteRouteItem(area: String, street: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Arequipa, $area", style = MaterialTheme.typography.bodyLarge)
                Text(text = street, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Rating y botón de favorito
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Ubicación"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "4.8")
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = { /* Acción del favorito */ }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
@Composable
fun RouteForm(
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre de la ruta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = from,
            onValueChange = { from = it },
            label = { Text("Desde") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = to,
            onValueChange = { to = it },
            label = { Text("Hasta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onCancel) {
                Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSave(name, from, to) }) {
                Text("Guardar")
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun FavPreview() {
    ProyectoFInalTheme {
        FavoriteRoute()
    }
}

