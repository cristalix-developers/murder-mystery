package me.func.murder.mod

import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import me.func.murder.MurderGame
import me.func.murder.getUser
import me.func.murder.map.MapType
import me.func.murder.user.Role
import me.func.murder.user.User
import org.bukkit.GameMode
import org.bukkit.entity.Player
import sun.audio.AudioPlayer.player
import java.util.UUID

class ModHelper(private val game: MurderGame) {

    companion object {
        fun sendCooldown(player: Player, text: String, ticks: Int) =
            ModTransfer().string(text).integer(ticks).send("murder:cooldown", player)
    }

    fun makeCorpse(corpse: Player) {
        game.players.map { game.userManager.getUser(it) }.forEach {
            val player = corpse.player!!
            val loc = player.location
            sendCorpse(player.name, player.uniqueId, it.player!!, loc.x, loc.y, loc.z)
        }
    }

    fun sendCorpse(name: String, uuid: UUID, to: Player, x: Double, y: Double, z: Double) {
        ModTransfer().string(name)
            .string("https://webdata.c7x.dev/textures/skin/$uuid")
            .string(uuid.toString())
            .double(x)
            .double(y + 3)
            .double(z)
            .boolean(true)
            .send("corpse", to)
    }

    fun loadMap(map: MapType) = game.players.forEach { ModTransfer().json(map.data).send("murder:map-load", it) }

    fun updateOnline() {
        val users = game.players.map { game.userManager.getUser(it) }
        val detectiveAlive = users.any { it.role == Role.DETECTIVE && it.player!!.gameMode != GameMode.SPECTATOR }
        val alive = users.filter { it.player!!.gameMode != GameMode.SPECTATOR }.size

        users.forEach {
            ModTransfer().boolean(detectiveAlive)
                .integer(alive - 1)
                .send(if (game.dbd) "dbd:update" else "murder:update", it.player)
        }
    }

    fun sendGlobalTitle(text: String) = game.players.forEach { Anime.title(it, text) }
}
