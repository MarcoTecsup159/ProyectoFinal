package com.example.proyectofinal.viewmodels

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.example.proyectofinal.Model.Empresa
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

    fun obtenerEmpresas(callback: (List<Empresa>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("empresas")
        val empresaList = mutableListOf<Empresa>()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresaList.clear()
                for (empresaSnapshot in snapshot.children) {
                    val empresa = empresaSnapshot.getValue(Empresa::class.java)
                    empresa?.let { empresaList.add(it) }
                }
                callback(empresaList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores si es necesario
            }
        })
    }

fun saveRouteToFirebase(context: Context, routeName: String, routePoints: List<LatLng>) {
    val database = FirebaseDatabase.getInstance()
    val routesRef = database.getReference("empresas/C4/rutas")

    val geocoder = Geocoder(context, Locale.getDefault())
    var originAddress = ""
    var destinationAddress = ""

    try {
        val originAddresses = geocoder.getFromLocation(routePoints.first().latitude, routePoints.first().longitude, 1)
        originAddress = originAddresses?.firstOrNull()?.getAddressLine(0) ?: ""

        val destinationAddresses = geocoder.getFromLocation(routePoints.last().latitude, routePoints.last().longitude, 1)
        destinationAddress = destinationAddresses?.firstOrNull()?.getAddressLine(0) ?: ""
    } catch (e: IOException) {
        Log.e("Geocoder", "Error getting address", e)
    }

    // Verificación de campos vacíos
    if (originAddress.isEmpty()) originAddress = "Origen desconocido"
    if (destinationAddress.isEmpty()) destinationAddress = "Destino desconocido"
    if (routeName.isBlank()) {
        Toast.makeText(context, "El nombre de la ruta no puede estar vacío", Toast.LENGTH_SHORT).show()
        return
    }

    val routeData = mapOf(
        "nombreRuta" to routeName,
        "origen" to originAddress,
        "destino" to destinationAddress,
        "puntosIntermedio" to routePoints.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
    )

    // Obtén el número de rutas existentes para generar un identificador único
    routesRef.get().addOnSuccessListener { snapshot ->
        val routeCount = snapshot.childrenCount.toInt() + 1
        val routeId = "R$routeCount"  // Formato de identificador R#

        // Guarda la nueva ruta bajo el identificador generado
        routesRef.child(routeId).setValue(routeData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Ruta guardada con éxito bajo la clave: $routeId")
            } else {
                Log.e("Firebase", "Error guardando la ruta: ${task.exception?.message}")
            }
        }
    }.addOnFailureListener { e ->
        Log.e("Firebase", "Error obteniendo el conteo de rutas: ${e.message}")
    }
}

    fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
        } catch (e: IOException) {
            "Error getting address"
        }
    }

    fun obtenerCoordenadas(empresaId: String, rutaId: String, callback: (List<Pair<Double, Double>>) -> Unit) {
        val rutaReference = FirebaseDatabase.getInstance()
            .getReference("empresas")
            .child(empresaId)
            .child("rutas")
            .child(rutaId)
            .child("puntosIntermedio")

        rutaReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coordenadas = mutableListOf<Pair<Double, Double>>()
                for (puntoSnapshot in snapshot.children) {
                    val latitud = puntoSnapshot.child("lat").getValue(Double::class.java)
                    val longitud = puntoSnapshot.child("lng").getValue(Double::class.java)
                    if (latitud != null && longitud != null) {
                        coordenadas.add(Pair(latitud, longitud))
                    }
                }
                callback(coordenadas)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores si es necesario
            }
        })
    }
