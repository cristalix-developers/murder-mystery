package me.func.murder.mod

import me.func.murder.MurderGame
import me.func.murder.getUser
import me.func.murder.map.MapType
import me.func.murder.user.Role
import me.func.murder.user.User
import org.bukkit.GameMode
import java.util.UUID

class ModHelper(private val game: MurderGame) {

    companion object {
        fun sendCooldown(user: User, text: String, ticks: Int) {
            ModTransfer().string(text).integer(ticks).send("murder:cooldown", user)
        }

        fun sendTitle(user: User, text: String) {
            ModTransfer().string(text).send("murder:title", user)
        }

        fun updateBalance(user: User) {
            ModTransfer().integer(user.stat.money).send("murder:balance", user)
        }
    }

    fun makeCorpse(corpse: User) {
        game.players.map { game.userManager.getUser(it) }.forEach {
            val player = corpse.player!!
            val loc = player.location
            sendCorpse(player.name, player.uniqueId, it, loc.x, loc.y, loc.z)
        }
    }

    fun sendCorpse(name: String, uuid: UUID, to: User, x: Double, y: Double, z: Double) {
        ModTransfer().string(name)
            .string("https://webdata.c7x.dev/textures/skin/$uuid")
            .string(uuid.toString())
            .double(x)
            .double(y + 3)
            .double(z)
            .boolean(true)
            .send("corpse", to)
    }

    fun loadMap(map: MapType) {
        game.players.map { game.userManager.getUser(it) }
            .forEach {
                ModTransfer()
                    .json(map.data)
                    .send("murder:map-load", it)
            }
    }

    fun updateOnline() {
        val users = game.players.map { game.userManager.getUser(it) }
        val detectiveAlive = users.any { it.role == Role.DETECTIVE && it.player!!.gameMode != GameMode.SPECTATOR }
        val alive = users.filter { it.player!!.gameMode != GameMode.SPECTATOR }.size

        users.forEach {
            ModTransfer()
                .boolean(detectiveAlive)
                .integer(alive - 1)
                .send("murder:update", it) // todo
            // .send(if (realm.realmId.realmName.contains("MUR")) "murder:update" else "dbd:update", it)
        }
    }

    fun sendGlobalTitle(text: String) {
        game.players.map { game.userManager.getUser(it.uniqueId) }.forEach { sendTitle(it, text) }
    }
}
