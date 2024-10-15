package com.example.proyectofinal.viewmodels

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.example.proyectofinal.Model.Empresa
import com.example.proyectofinal.Model.Ruta
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

fun saveRouteToFirebase(context: Context, empresaId: String, routeName: String, routePoints: List<LatLng>) {
    val database = FirebaseDatabase.getInstance()
    val routesRef = database.getReference("empresas/$empresaId/rutas")  // Guardar bajo la empresa seleccionada

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

    routesRef.get().addOnSuccessListener { snapshot ->
        val routeCount = snapshot.childrenCount.toInt() + 1
        val routeId = "R$routeCount"

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

fun obtenerRutas(empresaId: String, callback: (List<Ruta>) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance()
        .getReference("empresas")
        .child(empresaId)
        .child("rutas")

    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val rutaList = mutableListOf<Ruta>()
            for (rutaSnapshot in snapshot.children) {
                val ruta = rutaSnapshot.getValue(Ruta::class.java)
                ruta?.let { rutaList.add(it) }
            }
            callback(rutaList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Manejo de errores
        }
    })
}

fun updateRouteInFirebase(context: Context, oldEmpresaId: String, rutaId: String, newEmpresaId: String, newRouteName: String) {
    val database = FirebaseDatabase.getInstance()
    val oldRouteRef = database.getReference("empresas/$oldEmpresaId/rutas/$rutaId")
    val newRouteRef = database.getReference("empresas/$newEmpresaId/rutas/$rutaId")

    oldRouteRef.get().addOnSuccessListener { snapshot ->
        val routeData = snapshot.value as? Map<String, Any> ?: return@addOnSuccessListener

        // Actualizar el nombre de la ruta
        val updatedRouteData = routeData.toMutableMap().apply {
            put("nombreRuta", newRouteName)
        }

        // Guardar los datos actualizados en la nueva ubicación
        newRouteRef.setValue(updatedRouteData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Eliminar la ruta antigua si se movió a una nueva empresa
                if (oldEmpresaId != newEmpresaId) {
                    oldRouteRef.removeValue()
                }
                Log.d("Firebase", "Ruta actualizada con éxito")
            } else {
                Log.e("Firebase", "Error actualizando la ruta: ${task.exception?.message}")
            }
        }
    }.addOnFailureListener { e ->
        Log.e("Firebase", "Error obteniendo la ruta: ${e.message}")
    }
}