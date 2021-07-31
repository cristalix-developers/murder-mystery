package me.func.commons.mod

import me.func.commons.getByPlayer
import me.func.commons.user.Role
import me.func.commons.user.User
import org.bukkit.Bukkit
import org.bukkit.GameMode
import java.util.*

object ModHelper {

    fun makeCorpse(corpse: User) {
        Bukkit.getOnlinePlayers()
            .map { getByPlayer(it) }
            .forEach {
                val player = corpse.player!!
                val location = player.location
                sendCorpse(player.name, player.uniqueId, it, location.x, location.y, location.z)
            }
    }

    fun sendCorpse(name: String, uuid: UUID, to: User, x: Double, y: Double, z: Double) {
        ModTransfer()
            .string(name)
            .string("https://webdata.c7x.dev/textures/skin/$uuid")
            .string(uuid.toString())
            .double(x)
            .double(y + 3)
            .double(z)
            .boolean(true)
            .send("corpse", to)
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

    fun sendCooldown(user: User, text: String, ticks: Int) {
        ModTransfer()
            .string(text)
            .integer(ticks)
            .send("murder:cooldown", user)
    }

    fun sendGlobalTitle(text: String) {
        Bukkit.getOnlinePlayers().map { getByPlayer(it) }
            .forEach { sendTitle(it, text) }
    }

    fun sendTitle(user: User, text: String) {
        ModTransfer()
            .string(text)
            .send("murder:title", user)
    }

    fun updateBalance(user: User) {
        ModTransfer()
            .integer(user.stat.money)
            .send("murder:balance", user)
    }
}