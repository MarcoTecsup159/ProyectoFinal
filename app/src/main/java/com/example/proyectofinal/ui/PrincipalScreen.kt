package com.example.proyectofinal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PrincipalScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9100FF), // Color superior (morado)
                        Color(0xFF0047FF)  // Color inferior (azul)
                    )
                )
            )
            .clickable {
                // Navegar a la pantalla `home` al tocar cualquier lugar de la pantalla
                navController.navigate("home")
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "GoGuía 🌍\nEncuentra tu ruta ideal.\n¡Empieza tu viaje!",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
