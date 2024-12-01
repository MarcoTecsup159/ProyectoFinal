package com.example.proyectofinal

import android.location.Geocoder
import android.location.Address
import java.util.Locale

class LocationService(private val geocoder: Geocoder) {
    fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? {
        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses!!.isNotEmpty()) {
            addresses?.get(0)?.getAddressLine(0)
        } else {
            null
        }
    }
}
