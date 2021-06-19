import clepto.cristalix.Cristalix
import clepto.cristalix.WorldMeta
import ru.cristalix.core.map.BukkitWorldLoader
import java.util.concurrent.ExecutionException

object MapLoader {
    fun load(map: String?): WorldMeta? {
        // Загрузка карты с сервера BUIL-1
        val mapInfo = Cristalix.mapService().getLatestMapByGameTypeAndMapName("Murder", map)
            .orElseThrow { RuntimeException("Map Murder wasn't found in the MapService") }
        return try {
            val meta = WorldMeta(Cristalix.mapService().loadMap(mapInfo.latest, BukkitWorldLoader.INSTANCE).get())
            meta.world.setGameRuleValue("doDaylightCycle", "false")
            meta.world.setGameRuleValue("naturalRegeneration", "true")
            meta.world.time = 12000
            meta
        } catch (exception: Exception) {
            when (exception) {
                is InterruptedException,
                is ExecutionException -> {
                    exception.printStackTrace()
                    Thread.currentThread().interrupt()
                }
            }
            throw exception
        }
    }
}