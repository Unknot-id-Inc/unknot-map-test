package com.example.mapboxtest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMapScope
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.PointListValue
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.compose.style.sources.generated.rememberImageSourceState


data class MapOverlay(
    val bounds: CoordinateBounds,
    val name: String,
    val key: String,
    val url: String
) {
    constructor(swlat: Double, swlng: Double, nelat: Double, nelng: Double,
                name: String, key: String, url: String): this(
        bounds = CoordinateBounds(
            Point.fromLngLat(swlng, swlat),
            Point.fromLngLat(nelng, nelat)
        ),
        name = name,
        key = key,
        url = url,
    )
}

fun Overlay.toMapOverlay() = MapOverlay(
    bounds = CoordinateBounds(
        Point.fromLngLat(sw.lng, sw.lat),
        Point.fromLngLat(ne.lng, ne.lat)
    ),
    name = name,
    key = key,
    url = url,
)

fun Overlay.getBounds(): CoordinateBounds = CoordinateBounds(
    Point.fromLngLat(sw.lng, sw.lat),
    Point.fromLngLat(ne.lng, ne.lat)
)

@OptIn(MapboxExperimental::class, MapboxDelicateApi::class)
@Composable
fun MapboxMapScope.MapboxMapContent(
    state: MainUiState,
    locationProvider: UnknotLocationProvider
) {
    for (layer in overlays) {
        val imgSource = rememberImageSourceState {
            coordinates = layer.getBounds().toPointList()
            url = StringValue(layer.url)
        }

        val location by locationProvider.state.collectAsState()

        RasterLayer(sourceState = imgSource) {
            rasterOpacity = DoubleValue(
                if (state is MainUiState.Tracking && location?.level == layer.key)
                    0.8 else 0.5
            )
        }
    }
    /*val layers = mapOverlays.map {
        rememberImageSourceState {
            coordinates = it.bounds.toPointList()
            url = StringValue(it.url)
        }
    }

    layers.forEach {
        RasterLayer(sourceState = it) {
            rasterOpacity = DoubleValue(0.5)
        }
    }*/
}

fun CoordinateBounds.toPointList(): PointListValue =
    PointListValue(listOf(
        listOf(west(), north()),
        listOf(east(), north()),
        listOf(east(), south()),
        listOf(west(), south())
    ))

