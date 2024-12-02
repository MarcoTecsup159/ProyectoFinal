package com.example.proyectofinal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun PrincipalScreen(navController: NavController) {
    // Navegación automática
    LaunchedEffect(Unit) {
        delay(1000)
        navController.navigate("home")
    }

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
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "GoGuía 🛣️",
            color = Color.White,
            fontSize = 39.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 35.dp)
        )
        Text(
            text = "Encuentra tu ruta ideal.",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 35.dp)
        )

        // Indicador de carga que se muestra mientras se navega
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 40.dp),
            color = Color.White
        )
    }
}
