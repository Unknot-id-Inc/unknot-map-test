package com.example.mapboxtest

import androidx.compose.runtime.Composable
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState

@Composable
fun rememberGeoJsonSourceStates(vararg urls: String): List<GeoJsonSourceState> {
    return urls.map {
        rememberGeoJsonSourceState {
            data = GeoJSONData(it)
        }
    }
}

fun LatLng.toPoint() = Point.fromLngLat(lng, lat)
