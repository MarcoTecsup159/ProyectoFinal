package com.example.proyectofinal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.model.empresa
import com.example.proyectofinal.model.Ruta

@Composable
fun EmpresaCard(empresa: empresa, onRutaClick: (String, String) -> Unit) {
    var showRoutes by remember { mutableStateOf(false) }
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