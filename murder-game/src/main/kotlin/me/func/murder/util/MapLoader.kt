/*
package me.func.murder.util

import clepto.cristalix.Cristalix
import clepto.cristalix.WorldMeta
import me.func.commons.worldMeta
import ru.cristalix.core.map.BukkitWorldLoader

object MapLoader {

    fun load(name: String): WorldMeta {
        // Загрузка карты с сервера BUIL-1
        val mapInfo = Cristalix.mapService().getLatestMapByGameTypeAndMapName("Murder", name)
            .orElseThrow { RuntimeException("Map Murder wasn't found in the MapService") }
        worldMeta = WorldMeta(Cristalix.mapService().loadMap(mapInfo.latest, BukkitWorldLoader.INSTANCE).get())
        worldMeta.world.setGameRuleValue("doDaylightCycle", "false")
        worldMeta.world.setGameRuleValue("naturalRegeneration", "true")
        worldMeta.world.isAutoSave = false
        worldMeta.world.time = 21000
        return worldMeta
    }
}*/
