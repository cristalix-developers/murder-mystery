package me.func.commons.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import me.func.commons.donate.impl.*
import me.func.commons.map.MapType
import me.func.commons.mod.ModHelper
import me.func.commons.mod.ModTransfer
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PlayerConnection
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

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
    private var player: Player? = null
    override fun setPlayer(p0: Player?) {
        if (p0 != null) {
            player = p0
        }
    }

    override fun getPlayer(): Player? {
        return player
    }

    private var session: KensukeSession
    override fun getSession(): KensukeSession {
        return session
    }

    fun giveMoney(money: Int) {
        changeMoney(money)
    }

    fun minusMoney(money: Int) {
        changeMoney(-money)
    }

    fun sendPlayAgain(prefix: String, map: MapType) {
        player!!.spigot().sendMessage(
            *ComponentBuilder("\n$prefix §fИграть на Cristalix §dMurderMystery §e§lКЛИК\n")
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/next MUR"))
                .create()
        )
    }

    private fun changeMoney(dMoney: Int) {
        stat.money += dMoney
        ModTransfer()
            .integer(dMoney)
            .send("murder:money", this)
        ModHelper.updateBalance(this)
    }

    init {
        if (stat == null) {
            this.stat = Stat(
                UUID.fromString(session.userId), 0, 0, 0, 0, 0, 0, 2, true, 1,
                arrayListOf(), arrayListOf(
                    KillMessage.NONE,
                    StepParticle.NONE,
                    DeathImage.NONE,
                    NameTag.NONE,
                    Corpse.NONE,
                    ArrowParticle.NONE,
                    Mask.NONE,
                ),
                KillMessage.NONE,
                StepParticle.NONE,
                DeathImage.NONE,
                NameTag.NONE,
                Corpse.NONE,
                ArrowParticle.NONE,
                Mask.NONE,
                0,
                0,
                0,
                0,
                0,
                0,
                "",
            )
        } else {
            if (stat.activeKillMessage == null)
                stat.activeKillMessage = KillMessage.NONE
            if (stat.activeParticle == null)
                stat.activeParticle = StepParticle.NONE
            if (stat.activeDeathImage == null)
                stat.activeDeathImage = DeathImage.NONE
            if (stat.activeNameTag == null)
                stat.activeNameTag = NameTag.NONE
            if (stat.activeCorpse == null)
                stat.activeCorpse = Corpse.NONE
            if (stat.music == null)
                stat.music = true
            if (stat.lootboxOpenned == null)
                stat.lootboxOpenned = 0
            if (stat.moneyBooster == null)
                stat.moneyBooster = 1
            if (stat.achievement == null || stat.achievement.isEmpty())
                stat.achievement = arrayListOf()
            if (stat.donate == null || stat.donate.isEmpty())
                stat.donate = arrayListOf(
                    KillMessage.NONE,
                    StepParticle.NONE,
                    DeathImage.NONE,
                    NameTag.NONE,
                    Corpse.NONE,
                )
            if (stat.activeParticle == null)
                stat.arrowParticle = ArrowParticle.NONE
            if (stat.mask == null)
                stat.mask = Mask.NONE
            this.stat = stat
        }
        this.session = session
    }

    fun sendPacket(packet: Packet<*>) {
        if (player == null)
            return
        if (connection == null)
            connection = (player as CraftPlayer).handle.playerConnection
        connection?.sendPacket(packet)
    }
}