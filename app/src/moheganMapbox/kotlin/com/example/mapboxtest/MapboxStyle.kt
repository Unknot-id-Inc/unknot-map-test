package com.example.mapboxtest

import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.MapboxStyleComposable
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle

@Composable
@MapboxStyleComposable
fun MapboxStyle() {
    MapboxStandardStyle {
        show3dObjects = BooleanValue(false)
        showPointOfInterestLabels = BooleanValue(false)
    }
}