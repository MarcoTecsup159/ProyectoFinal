package com.example.proyectofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.navigation.AppScreen
import com.example.proyectofinal.ui.PrincipalScreen
import com.example.proyectofinal.ui.theme.ProyectoFInalTheme
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAiaswLBAIKRY-IuRcX-JfRQ6VNBQnUGvw")
        }
        FirebaseApp.initializeApp(this)
        setContent {
            ProyectoFInalTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "principal") {
        composable("principal") { PrincipalScreen(navController) }
        composable("home") { home(navController) }
    }
}

@Composable
fun home(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Pantalla principal (AppScreen)
        AppScreen()
    }
}
