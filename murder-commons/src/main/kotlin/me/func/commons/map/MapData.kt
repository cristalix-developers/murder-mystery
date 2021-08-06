package me.func.commons.map

import org.apache.logging.log4j.Marker

data class MapData(
    val title: String,
    val maxX: Double,
    val maxZ: Double,
    val playerTexturePath: String,
    val mapTexturePath: String,
    val textureSize: Double,
    val markers: List<Marker>
)