package com.example.proyectofinal.viewmodel

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("https://maps.googleapis.com/") // URL base correcta
    .addConverterFactory(GsonConverterFactory.create())
    .build()