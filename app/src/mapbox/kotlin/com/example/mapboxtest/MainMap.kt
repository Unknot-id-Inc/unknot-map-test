package com.example.mapboxtest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.ComposeMapInitOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.Filter
import com.mapbox.maps.extension.compose.style.layers.generated.FillExtrusionLayer
import com.mapbox.maps.extension.compose.style.layers.generated.FillExtrusionLayerState
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.inExpression
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.ViewportOptions
import com.mapbox.maps.plugin.viewport.viewport
import org.unknot.android_sdk.ForwardLocation
import kotlin.collections.forEach

@Composable
fun MainMap(
    state: MainUiState,
    locationProvider: UnknotLocationProvider
) {
    val viewportState = rememberMapViewportState {
        setCameraOptions(defaultCameraOptions)
    }

    MapboxMap(
        modifier = Modifier
            .fillMaxSize()
        ,
        composeMapInitOptions = with(LocalDensity.current) {
            remember {
                ComposeMapInitOptions(density)
            }
        },
        scaleBar = {},
        style = {
            MapboxStyle()
        },
        mapViewportState = viewportState,
    ) {
        MapEffect(Unit) { mapView ->
            val locP = object : LocationProvider {
                val consumers: MutableSet<LocationConsumer> = mutableSetOf()
                override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
                    consumers.add(locationConsumer)
                }

                override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
                    consumers.remove(locationConsumer)
                }
            }

            mapView.viewport.options = ViewportOptions.Builder().transitionsToIdleUponUserInteraction(false).build()
            mapView.location.run {
                //setLocationProvider(locationProvider)
                setLocationProvider(locP)

                updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = false)
                    enabled = true
                    pulsingEnabled = true
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.HEADING
                }
            }
            viewportState.transitionToFollowPuckState(
                FollowPuckViewportStateOptions.Builder()
                    .zoom(null)
                    .bearing(null)
                    .pitch(null)
                    .padding(null)
                    .build()
            )

            locationProvider.state.collect { loc ->
                if (loc != null) {
                    val mpoint = loc.toMapBoxPoint()
                    locP.consumers.forEach {
                        it.onLocationUpdated(mpoint)
                    }
                }
            }
        }

        MapboxMapContent(state, locationProvider)
    }
}

@Composable
fun extrude(
    source: GeoJsonSourceState,
    category: String,
    value: String,
    height: Double,
    color: Color,
    opacity: Double = 0.5,
    base: Double? = null
) = extrude(listOf(source), category, listOf(value), height, color, opacity, base)
@Composable
fun extrude(
    source: GeoJsonSourceState,
    category: String,
    value: List<String>,
    height: Double,
    color: Color,
    opacity: Double = 0.5,
    base: Double? = null
) = extrude(listOf(source), category, value, height, color, opacity, base)

@OptIn(MapboxExperimental::class)
@Composable
fun extrude(
    sources: List<GeoJsonSourceState>,
    filter: Filter,
    height: Double,
    color: Color,
    opacity: Double = 0.5,
    base: Double? = null,
    custom: FillExtrusionLayerState.() -> Unit = {}
) = sources.forEach { source ->
    FillExtrusionLayer(source) {
        this.filter = filter

        fillExtrusionHeight = DoubleValue(height)

        if (color == Color.Unspecified) {
            fillExtrusionColor = ColorValue(get {
                literal("fillColor")
            })
        }
        fillExtrusionOpacity = DoubleValue(opacity)
        fillExtrusionFloodLightColor = ColorValue(Color.Blue)
        fillExtrusionEmissiveStrength = DoubleValue(2.0)
        base?.let {
            fillExtrusionBase = DoubleValue(it)
        }

        fillExtrusionAmbientOcclusionIntensity = DoubleValue(0.5)
        fillExtrusionFloodLightIntensity = DoubleValue(0.5)

        custom()
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun extrude(
    sources: List<GeoJsonSourceState>,
    category: String,
    values: List<String>,
    height: Double,
    color: Color = Color.Unspecified,
    opacity: Double = 0.5,
    base: Double? = null
) = extrude(sources, Filter(inExpression { get(category); literal(values) }), height, color, opacity, base)
fun ForwardLocation.toMapBoxLocation() =
    Location.Builder()
        .latitude(latitude)
        .longitude(longitude)
        .timestamp(timestamp)
        .build()

fun ForwardLocation.toMapBoxPoint(): Point =
    Point.fromLngLat(longitude, latitude)

