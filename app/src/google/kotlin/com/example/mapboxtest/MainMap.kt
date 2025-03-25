package com.example.mapboxtest

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun MainMap(
    state: MainUiState,
    locationProvider: UnknotLocationProvider
) {
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    val location by locationProvider.state.collectAsState()

    // Center camera position on blue dot location
    LaunchedEffect(Unit) {
        locationProvider.state.collect {
            // If the camera move animation gets interrupted by a new update before it completes,
            // this throws an exception then stops updating entirely, so just catch and ignore.
            try {
                if (it != null) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLng(
                            it.getGoogleLatLng()
                        )
                    )
                }
            } catch (e: CancellationException) {
            }
        }
    }

    GoogleMap(
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false
        )
    ) {
        // Would show the blue dot this way, the reasonable way, but apparently you cannot animate
        // composables within GoogleMap. Animations are neat...
        /*location?.let {
            MarkerComposable(
                it.getGoogleLatLng(),
                anchor = Offset(0.5f, 0.5f),
                state = rememberUpdatedMarkerState(position = it.getGoogleLatLng())
            ) {
                BlueDot()
            }
        }*/

        overlays.forEach {
            GroundOverlay(
                position = remember { GroundOverlayPosition.create(it.bounds) },
                image = remember { BitmapDescriptorFactory.fromAsset(
                    it.url.replace("asset://", "")
                ) },
                transparency = if (location?.level == it.key) 0.3f else 0.7f
            )
        }
    }

    // So we do it this weird way, outside GoogleMap and adjusted based on camera position.
    // It works, but was it worth it...?
    MapOverlay(
        cameraPositionState = cameraPositionState
    ) {
        location?.let {
            // ha, try to make location updates smooth and see what happens, I dare you.
            // (it works until you manually pan the map, then it animates the blue dot the pan
            // distance. Looks wacky.)
            //val loc by animateIntOffsetAsState(it.getGoogleLatLng().toPx())
            val loc = it.getGoogleLatLng().toPx()
            BlueDot(
                modifier = Modifier
                    .offset { loc }
            )
        }
    }
}

val blueDotMaxSize = 75.dp
const val blueDotSize = 40f
val blueDotColor = Color(0xff4286f3)
const val blueDotStroke = 5f
const val blueDotMargin = 13f
const val blueDotHaloMargin = 80f

@Composable
fun BlueDot(
    modifier: Modifier = Modifier
) {

    val infiniteTransition = rememberInfiniteTransition()
    val haloRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        )
    )
    Canvas(
        modifier = Modifier
            .size(blueDotMaxSize)
            .offset(blueDotMaxSize / -2, blueDotMaxSize / -2)
            .then(modifier)
    ) {
        val csize = blueDotSize
        drawCircle(
            radius = csize,
            brush = Brush.radialGradient(
                0.0f to Color.Black.copy(0.7f),
                1f to Color.Transparent,
                radius = csize
            )
        )

        drawCircle(
            radius = size.width / 2 * haloRadius,
            color = blueDotColor.copy(0.5f * (1 - haloRadius))
        )

        drawCircle(
            radius = csize - blueDotMargin,
            color = Color(0xff4286f3)
        )

        drawCircle(
            radius = csize - blueDotMargin,
            color = Color.White,
            style = Stroke(
                width = blueDotStroke
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BlueDotPreview() {
    BlueDot()
}

// Wrapper functions to draw composables outside the map and still adjust for camera position.
@Composable
fun MapOverlay(
    cameraPositionState: CameraPositionState,
    content: @Composable MapOverlayScope.() -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        MapOverlayScope(cameraPositionState).content()
    }
}

class MapOverlayScope(
    private val cameraPositionState: CameraPositionState,
) {

    @Stable
    fun Modifier.align(alignment: Alignment) = this.composed {
        var intSize by remember { mutableStateOf(IntSize.Zero) }
        onSizeChanged { intSize = it }.offset {
            alignment.align(intSize, IntSize.Zero, LayoutDirection.Ltr)
        }
    }

    @Stable
    fun LatLng.toPx(): IntOffset {
        cameraPositionState.position
        return cameraPositionState.projection
            ?.toScreenLocation(this)
            ?.let { point ->
                IntOffset(point.x, point.y)
            } ?: IntOffset.Zero
    }
}