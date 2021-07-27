package me.func.murder.map

data class MapData(
    val title: String,
    val maxX: Double,
    val maxZ: Double,
    val playerTexturePath: String,
    val mapTexturePath: String,
    val textureSize: Double,
    val markers: List<Marker>
)