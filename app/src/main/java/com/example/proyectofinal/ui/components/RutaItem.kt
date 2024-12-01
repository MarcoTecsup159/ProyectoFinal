package com.example.proyectofinal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.model.empresa
import com.example.proyectofinal.model.Ruta
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun RutaItem(ruta: Ruta, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = ruta.nombreRuta ?: "Sin nombre")
            Text(text = "Origen: ${ruta.origen}")
            Text(text = "Destino: ${ruta.destino}")
        }
    }
}

fun obtenerEmpresasRealtime(onEmpresasChanged: (List<empresa>) -> Unit) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("empresas")
    databaseRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val empresasList = mutableListOf<empresa>()
            for (empresaSnapshot in snapshot.children) {
                val empresa = empresaSnapshot.getValue(empresa::class.java)
                empresa?.let { empresasList.add(it) }
            }
            onEmpresasChanged(empresasList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Manejo de errores si es necesario
        }
    })
}