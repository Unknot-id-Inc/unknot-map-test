package com.example.mapboxtest

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter

fun getAppVersion(context: Context): String {
    val versionName: String = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "Unknown version"
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        "Unknown version"
    }
    return versionName
}

@Composable
fun ImageIntrinsicSize(
    modifier: Modifier = Modifier,
    painter: Painter,
    colorFilter: ColorFilter? = null
) {
    val aspectRatio = painter.intrinsicSize.width / painter.intrinsicSize.height
    Box(modifier = modifier.fillMaxHeight().aspectRatio(aspectRatio)) {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = painter,
            colorFilter = colorFilter,
            contentDescription = null,
        )
    }
}

data class LatLng(val lat: Double, val lng: Double)
