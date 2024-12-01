package com.example.proyectofinal.model

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {

    fun getConnection(): Connection? {
        try {
            // Carga el controlador JDBC
            Class.forName("com.mysql.cj.jdbc.Driver")

            // Crea la URL de conexión
            val url = "jdbc:mysql://localhost:3306/pb?zeroDateTimeBehavior=CONVERT_TO_NULL"

            // Establece la conexión
            val connection = DriverManager.getConnection(url, "root", "")

            // Devuelve la conexión
            return connection
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }
}