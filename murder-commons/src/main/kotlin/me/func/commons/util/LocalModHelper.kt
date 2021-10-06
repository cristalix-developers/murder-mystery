package me.func.commons.util

import me.func.commons.getByPlayer
import me.func.commons.mod.ModTransfer
import me.func.commons.map.MapType
import me.func.commons.user.Role
import org.bukkit.Bukkit
import org.bukkit.GameMode

object LocalModHelper {

    fun loadMap(map: MapType) {
        Bukkit.getOnlinePlayers().map { getByPlayer(it) }
            .forEach {
                ModTransfer()
                    .json(map.data)
                    .send("murder:map-load", it)
            }
    }

    fun updateOnline() {
        val users = Bukkit.getOnlinePlayers().map { getByPlayer(it) }
        val detectiveAlive = users.any { it.role == Role.DETECTIVE && it.player!!.gameMode != GameMode.SPECTATOR }
        val alive = users.filter { it.player!!.gameMode != GameMode.SPECTATOR }.size
        users.forEach {
            ModTransfer()
                .boolean(detectiveAlive)
                .integer(alive)
                .send("murder:update", it)
        }
    }

}