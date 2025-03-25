package com.example.mapboxtest

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource

@Composable
fun SubLogo(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.dfw_logo_white),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorResource(R.color.dfw_logo))
    )
}