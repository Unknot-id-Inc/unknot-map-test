package com.example.mapboxtest

import com.google.android.gms.maps.model.LatLngBounds
import org.unknot.android_sdk.ForwardLocation

fun LatLng.toGoogleLatLng() = com.google.android.gms.maps.model.LatLng(lat, lng)

fun ForwardLocation.getGoogleLatLng() =
    com.google.android.gms.maps.model.LatLng(latitude, longitude)

val Overlay.bounds: LatLngBounds
    get() = LatLngBounds(sw.toGoogleLatLng(), ne.toGoogleLatLng())
