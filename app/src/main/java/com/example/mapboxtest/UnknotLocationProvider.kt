package com.example.mapboxtest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.unknot.android_sdk.ForwardLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class UnknotLocationProvider(/*scope: CoroutineScope*/)/*: com.example.mapboxtest.LocationProvider*/ {
    //val consumers: MutableSet<LocationConsumer> = mutableSetOf()
    //val channel = Channel<ForwardLocation>(Channel.CONFLATED)
    private val _state = MutableStateFlow<ForwardLocation?>(null)
    val state = _state.asStateFlow()

    /*init {
        scope.launch(Dispatchers.Main) {
            for (location in channel) {
                val mbloc = location.com.example.mapboxtest.toMapBoxPoint()
                consumers.forEach { it.onLocationUpdated(mbloc) }
            }
        }
    }*/

    fun updateLocation(location: ForwardLocation) {
        _state.update { location }
        //channel.trySend(location)
    }

    /*override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.add(locationConsumer)
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.remove(locationConsumer)
    }*/
}

fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val deltaLonRad = Math.toRadians(lon2 - lon1)

    val y = sin(deltaLonRad) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)

    var bearing = Math.toDegrees(atan2(y, x))
    bearing = (bearing + 360) % 360  // Normalize to 0-360 degrees

    return bearing
}
