package me.func.murder.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import me.func.murder.map.MapType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PlayerConnection
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.UUID

class User(session: KensukeSession, stat: Stat?) : IBukkitKensukeUser {

    private var connection: PlayerConnection? = null
    var tempLocation: Location? = null
    var bites = 0
    var lightTicks = 260
    var countdown = 0
    var out = false
    var hearts = 2
    var fuel = 0
    var role = Role.NONE
    var animationLock = false

    var stat: Stat
    private var _player: Player? = null

    override fun setPlayer(currentPlayer: Player) {
        _player = currentPlayer
    }

    override fun getPlayer(): Player = _player ?: error("player is null")

    private var session: KensukeSession

    override fun getSession() = session

    fun sendPlayAgain(prefix: String, map: MapType) {
        player!!.spigot().sendMessage(
            *ComponentBuilder("\n$prefix §fИграть на Cristalix §dMurderMystery §e§lКЛИК\n").event(
                ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/next MUR"
                )
            ).create()
        )
    }

    init {
        if (stat == null) {
            this.stat = Stat(
                UUID.fromString(session.userId), 0, 0, 0, 2, true, 1,
                0,
                0,
                0,
                0,
                0,
                0,
                "",
            )
        } else {
            this.stat = stat
        }
        this.session = session
    }

    fun sendPacket(packet: Packet<*>) {
        if (player == null) return
        if (connection == null) connection = (player as CraftPlayer).handle.playerConnection
        connection?.sendPacket(packet)
    }
}