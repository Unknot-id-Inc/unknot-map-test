package com.example.mapboxtest

import com.mapbox.maps.dsl.cameraOptions

val defaultCameraOptions = cameraOptions {
    zoom(defaultZoom)
    center(defaultCameraLocation.toPoint())
    pitch(0.0)
    bearing(0.0)
}