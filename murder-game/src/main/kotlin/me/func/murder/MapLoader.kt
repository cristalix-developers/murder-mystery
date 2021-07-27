package me.func.murder

import clepto.cristalix.Cristalix
import clepto.cristalix.WorldMeta
import me.func.murder.map.MapType
import ru.cristalix.core.map.BukkitWorldLoader
import java.util.concurrent.ExecutionException

val map = MapType.valueOf(System.getenv("MAP"))
lateinit var worldMeta: WorldMeta

class MapLoader {

    init {
        // Загрузка карты с сервера BUIL-1
        val mapInfo = Cristalix.mapService().getLatestMapByGameTypeAndMapName("Murder", map.address)
            .orElseThrow { RuntimeException("Map Murder wasn't found in the MapService") }
        worldMeta = try {
            val meta = WorldMeta(Cristalix.mapService().loadMap(mapInfo.latest, BukkitWorldLoader.INSTANCE).get())
            meta.world.setGameRuleValue("doDaylightCycle", "false")
            meta.world.setGameRuleValue("naturalRegeneration", "true")
            meta.world.time = 21000
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