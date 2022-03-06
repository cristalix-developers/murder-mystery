data class MapData(
    val title: String,
    var maxX: Double,
    var maxZ: Double,
    val playerTexturePath: String,
    var mapTexturePath: String,
    var textureSize: Double,
    val markers: List<Marker>
)
