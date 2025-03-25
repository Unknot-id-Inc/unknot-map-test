package com.example.mapboxtest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMapScope
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.Filter
import com.mapbox.maps.extension.compose.style.layers.generated.FillExtrusionLayer
import com.mapbox.maps.extension.style.expressions.dsl.generated.any
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.match

fun key2name(key: String): String = key

const val max_extrude = 10.0
@OptIn(MapboxExperimental::class)
@Composable
fun MapboxMapScope.MapboxMapContent(
    state: MainUiState,
    locationProvider: UnknotLocationProvider
) {


    val geoJsons = rememberGeoJsonSourceStates(
        "asset://geojsons/terminala-departures.json",
        "asset://geojsons/terminald-departures.json",
        "asset://geojsons/terminalb-departures.json",
        "asset://geojsons/terminalc-departures.json",
    )

    val skylinkGeoJsons = rememberGeoJsonSourceStates(
        "asset://geojsons/terminala-skylink.json",
        "asset://geojsons/terminalb-skylink.json",
        "asset://geojsons/terminalc-skylink.json",
        "asset://geojsons/terminald-skylink.json"
    )

    val location by locationProvider.state.collectAsState()
    val levelGeoJsons = if (state is MainUiState.Tracking && location?.level?.lowercase() == "skylink") {
        skylinkGeoJsons
    } else {
        geoJsons
    }

    levelGeoJsons.forEach { gj ->
        FillExtrusionLayer(gj) {
            filter = Filter(any {
                inExpression {
                    get("aiLayer")
                    literal(listOf("poi", "misc", "non-accessible"))
                }
                inExpression {
                    get("category")
                    literal(listOf(
                        "element.arrows",
                        "element.stairsfootprint",
                        "element.portalfootprint",
                        "element.escalatorfootprint"
                    ))
                }
            })
            //fillExtrusionHeight = DoubleValue(max_extrude)
            fillExtrusionHeight = DoubleValue(match {
                get("category")

                stop {
                    literal("element.arrows")
                    literal(4.5)
                }

                stop {
                    literal("element.portalfootprint")
                    literal(0.5)
                }

                literal(max_extrude)

                /*get("aiLayer")
                stop {
                    inExpression {
                        literal(listOf("poi", "misc", "non-accessible"))
                    }
                    max_extrude
                }*/
            })
            fillExtrusionColor = ColorValue(get {
                literal("fillColor")
            })
            fillExtrusionOpacity = DoubleValue(0.3)
            fillExtrusionFloodLightColor = ColorValue(Color.Blue)
            fillExtrusionEmissiveStrength = DoubleValue(2.0)
            fillExtrusionBase = DoubleValue(match {
                get("category")
                stop {
                    literal("element.arrows")
                    literal(4.0)
                }

                literal(0.0)
            })

            fillExtrusionAmbientOcclusionIntensity = DoubleValue(0.5)
            fillExtrusionFloodLightIntensity = DoubleValue(0.5)

            /*fillExtrusionLineWidth = DoubleValue(match {
                get("category")
                stop {
                    literal("element.portalfootprint")
                    literal(1.0)
                }

                literal(0)
            })*/
        }
    }
}