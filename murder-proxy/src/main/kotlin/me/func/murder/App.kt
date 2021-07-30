package me.func.murder

import clepto.bukkit.B
import clepto.cristalix.WorldMeta
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.commons.*
import me.func.commons.content.CustomizationNPC
import me.func.commons.content.Lootbox
import me.func.commons.content.TopManager
import me.func.commons.listener.GlobalListeners
import me.func.commons.user.User
import me.func.commons.util.MapLoader
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.awt.SystemColor.info
import java.util.*

lateinit var murder: App

class App : JavaPlugin(), Listener {

    override fun onEnable() {
        B.plugin = this
        murder = this
        Platforms.set(PlatformDarkPaper())
        B.events(this, GlobalListeners(), Lootbox())
        MurderInstance(this, { getUser(it) }, { getUser(it) }, MapLoader.load("hall"), 200)

        // Конфигурация реалма
        realm.isLobbyServer = true
        realm.servicedServers = arrayOf("MUR", "MURP")

        // Создание контента для лобби
        TopManager()
        Npcs.init(app)
        CustomizationNPC()
        // NPC поиска игры
        val balancer = PlayerBalancer("MUR", 16)
        var fixDoubleClick: Player? = null

        val npcLabel = worldMeta.getLabel("play")
        val npcArgs = npcLabel.tag.split(" ")
        npcLabel.setYaw(npcArgs[0].toFloat())
        npcLabel.setPitch(npcArgs[1].toFloat())
        Npcs.spawn(
            Npc.builder()
                .location(npcLabel.clone().subtract(0.0, 0.4, 0.0))
                .name("§f >> §b§lИГРАТЬ §f<<")
                .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                .skinUrl("https://webdata.c7x.dev/textures/skin/6f3f4a2e-7f84-11e9-8374-1cb72caa35fd")
                .skinDigest("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd")
                .type(EntityType.PLAYER)
                .onClick {
                    if (fixDoubleClick != null && fixDoubleClick == it)
                        return@onClick
                    balancer.accept(it)
                    fixDoubleClick = it
                }.build()
        )
    }

    private fun getUser(player: Player): User {
        return getUser(player.uniqueId)
    }

    private fun getUser(uuid: UUID): User {
        return userManager.getUser(uuid)
    }

    @EventHandler
    fun EntityDamageEvent.handle() {
        cancelled = true
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        B.postpone(25) { getUser(player).sendPlayAgain("§aПопробовать!") }
        if (Math.random() < 0.5)
            player.sendMessage(Formatting.fine("Рекомендуем сделать §eминимальную яркость §fи §bвключить звуки§f для полного погружения."))
    }
}