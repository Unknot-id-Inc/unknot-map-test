package com.example.mapboxtest

import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions

val defaultCameraOptions = cameraOptions {
    zoom(14.0)
    center(Point.fromLngLat(-97.04020040343407, 32.89654266867686))
    pitch(0.0)
    bearing(0.0)
}