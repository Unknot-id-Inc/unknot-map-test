package com.example.mapboxtest

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SubLogo(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier.height(45.dp),
        painter = painterResource(R.drawable.mohegan_sub_symbol),
        alignment = Alignment.CenterStart,
        contentDescription = null
    )
}