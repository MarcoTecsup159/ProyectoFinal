package com.example.proyectofinal.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.example.proyectofinal.model.empresa
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

fun obtenerEmpresas(callback: (List<empresa>) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance().getReference("empresas")
    val empresaList = mutableListOf<empresa>()

    databaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            empresaList.clear()
            for (empresaSnapshot in snapshot.children) {
                val empresa = empresaSnapshot.getValue(empresa::class.java)
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
    val routesRef = database.getReference("empresas/$empresaId/rutas") // Guardar bajo la empresa seleccionada

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

    // Generar un ID único para la ruta
    val routeId = "R" + System.currentTimeMillis() // Usar timestamp como identificador único

    routesRef.child(routeId).setValue(routeData).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Firebase", "Ruta guardada con éxito bajo la clave: $routeId")
        } else {
            Log.e("Firebase", "Error guardando la ruta: ${task.exception?.message}")
        }
    }.addOnFailureListener { e ->
        Log.e("Firebase", "Error al guardar la ruta: ${e.message}")
    }
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

fun obtenerCoordenadas(empresaId: String, rutaId: String, callback: (List<Pair<Double, Double>>, String) -> Unit)  {
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
            // Llamada al callback con coordenadas y rutaId como segundo parámetro
            callback(coordenadas, rutaId)
        }

        override fun onCancelled(error: DatabaseError) {
            // Manejo de errores si es necesario
        }
    })
}

fun obtenerRutasDeFirebase(callback: (List<Route>) -> Unit) {
    val rutas = mutableListOf<Route>()

    // Referencia a la ubicación de todas las empresas en Firebase
    val empresasReference = FirebaseDatabase.getInstance()
        .getReference("empresas")

    empresasReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Contador para saber cuándo se han procesado todas las rutas
            var rutasProcesadas = 0
            var totalRutas = 0

            // Primero, contamos el total de rutas que procesaremos
            for (empresaSnapshot in snapshot.children) {
                val rutasSnapshot = empresaSnapshot.child("rutas")
                totalRutas += rutasSnapshot.childrenCount.toInt()
            }

            // Iterar sobre cada empresa en el snapshot
            for (empresaSnapshot in snapshot.children) {
                val empresaId = empresaSnapshot.key ?: continue // Obtener el ID de la empresa
                val rutasSnapshot = empresaSnapshot.child("rutas")

                // Iterar sobre cada ruta de la empresa
                for (rutaSnapshot in rutasSnapshot.children) {
                    val color = empresaSnapshot.child("color").value as? String ?: "FFFFFF"
                    val rutaId = rutaSnapshot.key ?: continue // Obtener el ID de la ruta
                    val nombreRuta = rutaSnapshot.child("nombreRuta").getValue(String::class.java) ?: "Sin nombre" // Obtener el nombre de la ruta
                    obtenerCoordenadas(empresaId, rutaId) { puntos, _ ->
                        val origen = puntos.firstOrNull()?.let { LatLng(it.first, it.second) } ?: LatLng(0.0, 0.0)
                        val destino = puntos.lastOrNull()?.let { LatLng(it.first, it.second) } ?: LatLng(0.0, 0.0)

                        // Asegúrate de que estás usando 'routePoints' aquí
                        val ruta = Route(
                            id = rutaId,
                            routePoints = puntos.map { LatLng(it.first, it.second) }, // Cambia a routePoints
                            origen = origen,
                            destino = destino,
                            nombreRuta = nombreRuta, // Añadir el nombre de la ruta
                            empresaColor = color
                        )

                        rutas.add(ruta)
                        rutasProcesadas++

                        // Verifica si se han procesado todas las rutas
                        if (rutasProcesadas == totalRutas) {
                            callback(rutas)
                        }
                    }
                }
            }

            // Manejo de caso donde no hay rutas
            if (totalRutas == 0) {
                callback(rutas) // Retornar lista vacía si no hay rutas
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Manejo de errores si es necesario
            callback(emptyList()) // Retornar lista vacía en caso de error
        }
    })
}