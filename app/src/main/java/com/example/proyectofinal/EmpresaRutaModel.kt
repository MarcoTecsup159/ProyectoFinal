package com.example.proyectofinal

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