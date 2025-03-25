package com.example.mapboxtest

data class Overlay(
    val sw: LatLng,
    val ne: LatLng,
    val name: String,
    val key: String,
    val url: String
)
val overlays = listOf(
    Overlay(
        LatLng(41.492144939593224, -72.08978454120805),
        LatLng(41.49289682741335, -72.08797356660835),
        "B2", "2101010000",
        "asset://map-overlays/1-1-B2-map.png"
    ),
    Overlay(
        LatLng(41.490837688662225, -72.09386821643287),
        LatLng(41.49276431858852,-72.08888617541442),
        "L1", "2101030000",
        "asset://map-overlays/1-1-L1-map.png"
    ),
    Overlay(
        LatLng(41.49190640723417, -72.08929534243309),
        LatLng(41.493011337039185, -72.08774307566071),
        "L12", "2101140000",
        "asset://map-overlays/1-1-L12-map.png"
    ),
    Overlay(
        LatLng(41.49185214728839, -72.0895305313346),
        LatLng(41.493064187635724, -72.08750788107754),
        "L15", "2101150000",
        "asset://map-overlays/1-1-L15-map.png"
    ),
    Overlay(
        LatLng(41.49083449421348, -72.09348585559924),
        LatLng(41.492759665276814, -72.08850362117937),
        "L2", "2101040000",
        "asset://map-overlays/1-1-L2-map.png"
    )
)

fun key2name(key: String): String = overlays.firstOrNull { it.key == key }?.name ?: key
