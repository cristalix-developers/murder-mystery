package me.func.murder.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class User(session: KensukeSession, stat: Stat?) : IBukkitKensukeUser {
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

    fun sendPlayAgain(prefix: String) {
        player.spigot().sendMessage(
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
}
