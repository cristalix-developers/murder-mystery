package me.func.murder.util

import me.func.commons.getByPlayer
import me.func.commons.mod.ModTransfer
import me.func.murder.map.MapType
import org.bukkit.Bukkit

object LocalModHelper {

    fun loadMap(map: MapType) {
        Bukkit.getOnlinePlayers().map { getByPlayer(it) }
            .forEach {
                ModTransfer()
                    .json(map.data)
                    .send("murder:map-load", it)
            }
    }

}