package com.example.mapboxtest

import com.google.android.gms.maps.model.CameraPosition

val defaultCameraPosition = CameraPosition.fromLatLngZoom(
    defaultCameraLocation.toGoogleLatLng(),
    defaultZoom.toFloat()
)