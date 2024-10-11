package com.example.proyectofinal

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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