package com.example.proyectofinal.utils

import android.location.Location
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.calculateDistance
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.calculateRouteDistance
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.decodePolyline
import com.example.proyectofinal.utils.RouteUtils.GeoUtils.distanceBetween
import com.example.proyectofinal.utils.RouteUtils.calcularRutaMasEficiente
import com.example.proyectofinal.utils.RouteUtils.findClosestPointOnBisector
import com.example.proyectofinal.utils.RouteUtils.findClosestPoints
import com.example.proyectofinal.utils.RouteUtils.obtenerRutasCercanas
import com.example.proyectofinal.viewmodel.Route
import com.example.proyectofinal.viewmodel.encontrarPuntoMasCercano
import com.example.proyectofinal.viewmodel.getCurrentLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*
import kotlin.test.assertTrue


class FuntionMateTest {
    //decodePolyline
    @Test
    fun `decodePolyline_con_ingreso_valido`() {
        // Entrada
        val polyline = "gfo}EtohhUxD@bAxJmGF"

        // Salida esperada
        val expected = listOf(
            LatLng(38.5, -120.2),
            LatLng(40.7, -120.95),
            LatLng(43.252, -126.453)
        )

        // Llamar a la función y comparar
        val result = decodePolyline(polyline)
        assertEquals(expected, result)
    }

    @Test
    fun `decodePolyline_con_ingreso_vacio`() {
        // Entrada
        val polyline = ""

        // Salida esperada
        val expected = emptyList<LatLng>()

        // Llamar a la función y comparar
        val result = decodePolyline(polyline)
        assertEquals(expected, result)
    }

    @Test
    fun `decodePolyline_with_malformed_input_returns_empty_list_or_throws_exception`() {
        // Entrada
        val polyline = "invalid_polyline"

        // Verificar que no crashea y devuelve vacío
        val result = decodePolyline(polyline)
        assertEquals(emptyList<LatLng>(), result)
    }

    //distanceBetween

    @Test
    fun `distanceBetween_two_identical_points_is_zero`() {
        // Entrada: Misma ubicación
        val pointA = LatLng(40.748817, -73.985428) // Empire State Building

        // Ejecución
        val result = distanceBetween(pointA, pointA)

        // Verificación
        assertEquals(0.00f, result, 0.01f)
    }

    @Test
    fun `distanceBetween_known_points_is_correct`() {
        // Entrada: Distancia conocida entre dos ubicaciones
        val pointA = LatLng(40.748817, -73.985428) // Empire State Building
        val pointB = LatLng(37.774929, -122.419416) // San Francisco, CA

        // Distancia esperada (aproximada en metros, calculada manualmente o con herramientas confiables)
        val expectedDistance = 4129000.0f // En metros (4129 km)

        // Ejecución
        val result = distanceBetween(pointA, pointB)

        // Verificación
        assertEquals(expectedDistance, result, 50000.0f)
    }

    @Test
    fun `distanceBetween_points_with_negative_coordinates_is_correct`() {
        // Entrada: Puntos en el hemisferio sur y occidental
        val pointA = LatLng(-34.603722, -58.381592) // Buenos Aires, Argentina
        val pointB = LatLng(-33.448890, -70.669265) // Santiago, Chile

        // Distancia esperada (aproximada en metros, calculada manualmente o con herramientas confiables)
        val expectedDistance = 1137000.0f // En metros (1137 km)

        // Ejecución
        val result = distanceBetween(pointA, pointB)

        // Verificación
        assertEquals(expectedDistance, result, 5000.0f)
    }

    //FindClosestPoints

    @Test
    fun `route_with_enough_points_returns_correct_closest_points`() {
        val route = Route(
            id = "1",
            routePoints = listOf(
                LatLng(40.748817, -73.985428),
                LatLng(37.774929, -122.419416),
                LatLng(34.052235, -118.243683),
                LatLng(41.878113, -87.629799),
                LatLng(47.606209, -122.332069)
            ),
            origen = LatLng(40.748817, -73.985428),
            destino = LatLng(47.606209, -122.332069),
            nombreRuta = "Test Route"
        )
        val userLocation = LatLng(40.712776, -74.005974)
        val expectedClosestPoints = listOf(
            LatLng(40.748817, -73.985428),
            LatLng(41.878113, -87.629799)
        )

        val result = findClosestPoints(route, userLocation, numPoints = 2)
        assertEquals(expectedClosestPoints, result)
    }

    @Test
    fun `route_with_fewer_than_numPoints_returns_empty_list`() {
        val route = Route(
            id = "1",
            routePoints = listOf(
                LatLng(40.748817, -73.985428), // Point A
            ),
            origen = LatLng(40.748817, -73.985428),
            destino = LatLng(40.748817, -73.985428),
            nombreRuta = "Short Route",
        )
        val userLocation = LatLng(40.750000, -73.986000)

        val result = findClosestPoints(route, userLocation, numPoints = 2)
        assertEquals(emptyList<LatLng>(), result)
    }

    @Test
    fun `user_location_exactly_matches_a_route_point`() {
        val route = Route(
            id = "1",
            routePoints = listOf(
                LatLng(40.748817, -73.985428),
                LatLng(40.712776, -74.005974),
                LatLng(37.774929, -122.419416)
            ),
            origen = LatLng(40.748817, -73.985428),
            destino = LatLng(37.774929, -122.419416),
            nombreRuta = "Exact Match Route"
        )
        val userLocation = LatLng(40.712776, -74.005974)
        val expectedClosestPoints = listOf(
            LatLng(40.712776, -74.005974),
            LatLng(40.748817, -73.985428)
        )

        val result = findClosestPoints(route, userLocation, numPoints = 2)
        assertEquals(expectedClosestPoints, result)
    }

    @Test
    fun `user_location_is_equidistant_from_multiple_points`() {
        val route = Route(
            id = "1",
            routePoints = listOf(
                LatLng(40.748817, -73.985428),
                LatLng(40.730610, -73.935242),
                LatLng(40.712776, -74.005974)
            ),
            origen = LatLng(40.748817, -73.985428),
            destino = LatLng(40.750000, -73.986000),
            nombreRuta = "Equidistant Route"
        )
        val userLocation = LatLng(40.730000, -73.975000)

        val result = findClosestPoints(route, userLocation, numPoints = 2)
        assertEquals(2, result.size)
    }

    //findClosestPointOnBisector

    @Test
    fun `two_well-defined_points_return_correctly_projected_bisector`() {
        val closestPoints = listOf(
            LatLng(40.748817, -73.985428), // Point A
            LatLng(40.752817, -73.989428)  // Point B
        )
        val userLocation = LatLng(40.750000, -73.987000) // Slightly off the midpoint
        val routeName = "Test Route"
        val expectedProjection = LatLng(40.750000, -73.987000) // Projection halfway towards the user

        val result = findClosestPointOnBisector(userLocation, closestPoints, routeName)
        assertEquals(expectedProjection, result)
    }

    @Test
    fun `user_at_midpoint_returns_midpoint`() {
        val closestPoints = listOf(
            LatLng(40.748817, -73.985428), // Point A
            LatLng(40.752817, -73.989428)  // Point B
        )
        val userLocation = LatLng(40.750817, -73.987428) // Exact midpoint
        val routeName = "Midpoint Test Route"
        val expectedProjection = LatLng(40.750817, -73.987428) // User location matches midpoint

        val result = findClosestPointOnBisector(userLocation, closestPoints, routeName)
        assertEquals(expectedProjection, result)

    }

    @Test
    fun `less_than_two_points_logs_error_and_returns_user_location`() {
        val closestPoints = listOf(
            LatLng(40.748817, -73.985428) // Single point
        )
        val userLocation = LatLng(40.750000, -73.987000)
        val routeName = "Single Point Route"

        val result = findClosestPointOnBisector(userLocation, closestPoints, routeName)
        assertEquals(userLocation, result)
    }

    //calculateRouteDistance

    @Test
    fun emptyListReturnsZero() {
        val points = emptyList<LatLng>()
        val result = calculateRouteDistance(points)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun singlePointReturnsZero() {
        val points = listOf(LatLng(40.748817, -73.985428))
        val result = calculateRouteDistance(points)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun multiplePointsCalculatesCorrectly() {
        val points = listOf(
            LatLng(40.748817, -73.985428),
            LatLng(40.689247, -74.044502),
            LatLng(40.730610, -73.935242)
        )
        val result = calculateRouteDistance(points)
        assertEquals(28.86, result)
    }

    @Test
    fun differentHemispheresCalculatesCorrectly() {
        val points = listOf(
            LatLng(-33.865143, 151.209900),
            LatLng(51.507351, -0.127758)
        )
        val result = calculateRouteDistance(points)
        assertEquals(16991.48, result, 1.0)
    }

    //calculateDistance

    @Test
    fun identicalPointsReturnsZero() {
        val lat1 = 40.748817
        val lon1 = -73.985428
        val lat2 = 40.748817
        val lon2 = -73.985428
        val result = calculateDistance(lat1, lon1, lat2, lon2)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun antipodalPointsCalculateCorrectly() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = 0.0
        val lon2 = 180.0
        val result = calculateDistance(lat1, lon1, lat2, lon2)
        assertEquals(20015e3, result, 1000.0)
    }

    @Test
    fun sameLatitudeCalculatesCorrectly() {
        val lat1 = 51.507351
        val lon1 = -0.127758
        val lat2 = 51.507351
        val lon2 = 2.352222
        val result = calculateDistance(lat1, lon1, lat2, lon2)
        assertEquals(343800.0, result, 100.0)
    }

    @Test
    fun sameLongitudeCalculatesCorrectly() {
        val lat1 = 40.748817
        val lon1 = -73.985428
        val lat2 = 34.052235
        val lon2 = -73.985428
        val result = calculateDistance(lat1, lon1, lat2, lon2)
            assertEquals(740000.0, result, 1000.0)
    }

    //calcularRutaMasEficiente

    @Test
    fun usuarioYDestinoEnRangoDevuelveEsaRuta() {
        val usuario = LatLng(40.748817, -73.985428)
        val destino = LatLng(40.751000, -73.987500)
        val rutas = listOf(
            Route(
            id = "ruta 1",
            routePoints = listOf(
            LatLng(40.748900, -73.985500),
            LatLng(40.751200, -73.987600)
            ),
            origen = LatLng(40.748900, -73.985500),
            destino = LatLng(40.751200, -73.987600),
            nombreRuta = "ruta 1"
            )
        )

        val resultado = calcularRutaMasEficiente(usuario, destino, rutas)
        assertNotNull(resultado)
        assertEquals("ruta 1", resultado?.nombreRuta)
    }

    @Test
    fun usuarioYDestinoEnRangoDeMultiplesRutasDevuelveLaMasCorta() {
        val usuario = LatLng(40.748817, -73.985428)
        val destino = LatLng(40.751000, -73.987500)
        val rutas = listOf(
            Route(id = "Ruta 1",
                routePoints = listOf(
                    LatLng(40.748900, -73.985500),
                    LatLng(40.751200, -73.987600)
                ),
                origen = LatLng(40.748900, -73.985500),
                destino = LatLng(40.751200, -73.987600),
                nombreRuta = "Ruta 1"),
            Route(id = "Ruta 2",
                routePoints = listOf(
                    LatLng(40.748850, -73.985450),
                    LatLng(40.750800, -73.987200)
                ),
                origen = LatLng(40.748850, -73.985450),
                destino = LatLng(40.750800, -73.987200),
                nombreRuta = "Ruta 2")
        )
        val resultado = calcularRutaMasEficiente(usuario, destino, rutas)
        assertNotNull(resultado)
        assertEquals("Ruta 2", resultado?.nombreRuta)
    }

    @Test
    fun usuarioFueraDeRangoDevuelveNull() {
        val usuario = LatLng(50.748817, -80.985428)
        val destino = LatLng(50.751000, -80.987500)
        val rutas = listOf(
            Route(id = "Ruta 1",
                routePoints = listOf(
                    LatLng(40.748900, -73.985500),
                    LatLng(40.751200, -73.987600)
                ),
                origen =  LatLng(40.748900, -73.985500),
                destino =  LatLng(40.748900, -73.985500),
                nombreRuta = "Ruta 1"),
        )

        val resultado = calcularRutaMasEficiente(usuario, destino, rutas)
        assertNull(resultado)
    }
    @Test
    fun usuarioDentroDeRangoSinPuntosSuficientesDevuelveNull() {
        val usuario = LatLng(40.748817, -73.985428)
        val destino = LatLng(40.751000, -73.987500)

        val rutas = listOf(
            Route(id = "Ruta 1",
                routePoints = listOf(
                    LatLng(40.748900, -73.985500)
                ),
                origen = LatLng(40.748900, -73.985500),
                destino = LatLng(40.748900, -73.985500),
                nombreRuta = "Ruta 1"),
        )

        val resultado = calcularRutaMasEficiente(usuario, destino, rutas)
        assertNull(resultado)
    }

    //encontrarPuntoMasCercano

    @Test
    fun `deberia_devolver_el_punto_mas_cercano_en_una_lista_de_coordenadas`() {
        // Datos de entrada
        val currentLocation = LatLng(0.0, 0.0)
        val coordenadas = listOf(
            Pair(1.0, 1.0),
            Pair(2.0, 2.0),
            Pair(0.5, 0.5)
        )

        // Resultado esperado
        val expected = LatLng(0.5, 0.5)

        // Llamada a la función
        val resultado = encontrarPuntoMasCercano(currentLocation, coordenadas)

        // Verificación
        assertEquals(expected, resultado)
    }

    @Test
    fun `deberia_devolver_null_cuando_la_lista_de_coordenadas_esta_vacia`() {
        // Datos de entrada
        val currentLocation = LatLng(0.0, 0.0)
        val coordenadas = emptyList<Pair<Double, Double>>()

        // Resultado esperado
        val expected = null

        // Llamada a la función
        val resultado = encontrarPuntoMasCercano(currentLocation, coordenadas)

        // Verificación
        assertEquals(expected, resultado)
    }

    @Test
    fun `deberia_devolverel_primer_punto_cuando_hay_varios_a_la_misma_distancia`() {
        // Datos de entrada
        val currentLocation = LatLng(0.0, 0.0)
        val coordenadas = listOf(
            Pair(1.0, 1.0),
            Pair(1.0, -1.0),
            Pair(-1.0, -1.0)
        )

        // Resultado esperado
        val expected = LatLng(1.0, 1.0) // El primero en la lista

        // Llamada a la función
        val resultado = encontrarPuntoMasCercano(currentLocation, coordenadas)

        // Verificación
        assertEquals(expected, resultado)
    }

    //getCurrentLocation

    @Test
    fun testGetCurrentLocation() {
        // Crear el objeto LatLng que representa la ubicación
        val latLng = LatLng(40.748817, -73.985428)

        // Crear el mock para Location
        val location = mock<Location>()
        `when`(location.latitude).thenReturn(latLng.latitude)
        `when`(location.longitude).thenReturn(latLng.longitude)

        // Crear el mock para FusedLocationProviderClient
        val fusedLocationClient = mock<FusedLocationProviderClient>()
        // Crear un Task simulado que retorna la location mockeada
        val task = Tasks.forResult(location) // Usar Tasks.forResult()

        // Simulamos el comportamiento de 'lastLocation'
        `when`(fusedLocationClient.lastLocation).thenReturn(task)

        // Crear el mock para el callback
        val onLocationReceived: (LatLng) -> Unit = mock()

        // Llamar a la función que estamos probando
        getCurrentLocation(fusedLocationClient, onLocationReceived)

        // Verificar que el callback fue invocado con las coordenadas correctas
        verify(onLocationReceived).invoke(latLng)
    }


    @Test
    fun testObtenerRutasCercanas_enRango() {
        val usuario = LatLng(40.748817, -73.985428)
        val rutas = listOf(
            Route(id = "Ruta 1",
                routePoints = listOf(
                LatLng(40.748900, -73.985500),
                LatLng(40.751200, -73.987600)
            ),
                origen = LatLng(40.748900, -73.985500),
                destino = LatLng(40.751200, -73.987600),
                nombreRuta = "Ruta 1"
            ),
            Route(id = "Ruta 2",
                routePoints =  listOf(
                LatLng(41.748900, -74.985500),
                LatLng(41.751200, -74.987600)
            ),
                origen = LatLng(41.748900, -74.985500),
                destino = LatLng(41.751200, -74.987600),
                nombreRuta = "Ruta 2"
            )
        )

        val rutasCercanas = obtenerRutasCercanas(usuario, rutas)

        assertEquals(1, rutasCercanas.size)
        assertEquals("Ruta 1", rutasCercanas.first().nombreRuta)
    }

    @Test
    fun testObtenerRutasCercanas_fueraDeRango() {
        val usuario = LatLng(50.748817, -73.985428) // Usuario fuera del rango
        val rutas = listOf(
            Route(id = "Ruta 1",
                routePoints =  listOf(
                LatLng(40.748900, -73.985500),
                LatLng(40.751200, -73.987600)
            ),
                origen = LatLng(40.748900, -73.985500),
                destino = LatLng(40.751200, -73.987600),
                nombreRuta = "Ruta 1"
            )
        )

        val rutasCercanas = obtenerRutasCercanas(usuario, rutas)

        assertTrue(rutasCercanas.isEmpty(), "No debería devolver ninguna ruta fuera de rango")
    }

    @Test
    fun testObtenerRutasCercanas_conRutaVacia() {
        val usuario = LatLng(40.748817, -73.985428)
        val rutas = listOf(
            Route(id = "Ruta 1",
                routePoints =  listOf(
                LatLng(40.748900, -73.985500),
                LatLng(40.751200, -73.987600)
            ),
                origen = LatLng(40.748900, -73.985500),
                destino = LatLng(40.751200, -73.987600),
                nombreRuta = "Ruta 1"
            ),
            Route(id = "Ruta 2", routePoints = emptyList(),
                origen = LatLng(0.0,0.0),
                destino = LatLng(0.0,0.0),
                nombreRuta = "Ruta 2"
            ) // Ruta con puntos vacíos
        )

        val rutasCercanas = obtenerRutasCercanas(usuario, rutas)

        assertEquals(1, rutasCercanas.size)
        assertEquals("Ruta 1", rutasCercanas.first().nombreRuta)
    }

}

