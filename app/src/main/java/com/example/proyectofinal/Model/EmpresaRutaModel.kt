package com.example.proyectofinal.Model

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

data class Empresa(
    val color: String? = null,
    val nombre: String? = null,
    val unidadNegocio: String? = null,
    val zonaCobertura: List<String>? = null,
    val rutas: Map<String, Ruta>? = null
)

data class Ruta(
    val destino: String? = null,
    val nombreRuta: String? = null,
    val origen: String? = null,
    val puntosIntermedio: List<PuntoIntermedio>? = null
)

data class PuntoIntermedio(
    val lat: Double? = null,
    val lng: Double? = null
)
