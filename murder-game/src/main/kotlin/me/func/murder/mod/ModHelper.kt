package me.func.murder.mod

import me.func.murder.app
import me.func.murder.map
import me.func.murder.map.MapType
import me.func.murder.user.Role
import me.func.murder.user.User
import org.bukkit.Bukkit
import org.bukkit.GameMode
import java.util.*

object ModHelper {

    fun makeCorpse(corpse: User) {
        Bukkit.getOnlinePlayers()
            .map { app.getUser(it) }
            .forEach {
                val player = corpse.player!!
                val location = player.location
                sendCorpse(player.uniqueId, it, location.x, location.y, location.z)
            }
    }

    fun sendCorpse(uuid: UUID, to: User, x: Double, y: Double, z: Double) {
        ModTransfer()
            .string(uuid.toString())
            .string(".")
            .string("https://webdata.c7x.dev/textures/skin/$uuid")
            .string(uuid.toString())
            .double(x)
            .double(y + 3)
            .double(z)
            .boolean(true)
            .send("corpse", to)
    }

    fun loadMap(map: MapType) {
        Bukkit.getOnlinePlayers().map { app.getUser(it) }
            .forEach {
                ModTransfer()
                    .json(map.data)
                    .send("murder:map-load", it)
            }
    }

    fun update() {
        val users = Bukkit.getOnlinePlayers().map { app.getUser(it) }
        val detectiveAlive = users.any { it.role == Role.DETECTIVE && it.player!!.gameMode != GameMode.SPECTATOR }
        val alive = users.filter { it.player!!.gameMode != GameMode.SPECTATOR }.size
        users.forEach {
            ModTransfer()
                .boolean(detectiveAlive)
                .integer(alive)
                .send("murder:update", it)
        }
    }

    fun sendCooldown(user: User, text: String, ticks: Int) {
        ModTransfer()
            .string(text)
            .integer(ticks)
            .send("murder:cooldown", user)
    }

    fun sendGlobalTitle(text: String) {
        Bukkit.getOnlinePlayers().map { app.getUser(it) }
            .forEach { sendTitle(it, text) }
    }

    fun sendTitle(user: User, text: String) {
        ModTransfer()
            .string(text)
            .send("murder:title", user)
    }
}