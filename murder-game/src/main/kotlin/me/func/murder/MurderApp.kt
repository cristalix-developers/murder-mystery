package me.func.murder

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.DefaultGameNode
import dev.implario.games5e.node.GameCreator
import dev.implario.games5e.node.linker.SessionBukkitLinker
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.Arcade
import me.func.battlepass.quest.ArcadeType
import me.func.mod.conversation.ModLoader
import me.func.murder.command.AdminCommand
import me.func.murder.user.Stat
import me.func.murder.user.User
import net.minecraft.server.v1_12_R1.HandshakeListener.gson
import net.minecraft.server.v1_12_R1.SoundEffects.id
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.BukkitPlatform
import ru.cristalix.core.CoreApi
import ru.cristalix.core.CoreCredentials
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.SocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import ru.cristalix.npcs.server.Npcs
import java.lang.reflect.Constructor


lateinit var app: MurderApp

class MurderApp : JavaPlugin(), Listener {
    private val core = CoreApi.get()
    private val statScope = Scope("murder-mystery", Stat::class.java)

    private lateinit var userManager: BukkitUserManager<User>
    private lateinit var kensuke: Kensuke

    lateinit var node: DefaultGameNode

    override fun onEnable() {
        app = this
        B.plugin = this

        core.init(BukkitPlatform(Bukkit.getServer(), Bukkit.getLogger(), this))
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))

        Arcade.start(ArcadeType.MUR)

        EntityDataParameters.register()
        Platforms.set(PlatformDarkPaper())

        getServer().pluginManager.registerEvents(this, this)

        ModLoader.loadAll("mods")
        Npcs.init(this)

        userManager = BukkitUserManager(setOf(statScope),
            { session, context -> User(session, context.getData(statScope)) },
            { user, context -> context.store(statScope, user.stat) }).apply { isOptional = true }

        kensuke = BukkitKensuke.setup(this).apply {
            addGlobalUserManager(userManager)
            globalRealm = "MUR-${(Math.random() * 1000).toInt()}"
        }

        node = DefaultGameNode()
        node.supportedImagePrefixes += listOf("murder", "dbd")
        node.linker = SessionBukkitLinker.link(node)

        node.gameCreator = GameCreator { gameId, image, settings ->
            MurderGame(
                gameId,
                gson.fromJson(settings, MurderSettings::class.java),
                kensuke,
                userManager,
                statScope,
                image == "dbd"
            )
        }

        Bukkit.getScheduler().runTaskTimer(this, {
            Bukkit.getWorlds().firstOrNull { it.players.isEmpty() }?.let {
                Bukkit.unloadWorld(it, false)
                println("One world was deleted! Active games: ${app.node.runningGames.size}, loaded world count: " +
                        "${Bukkit.getWorlds().size}")
            }
        }, 120, 20 * 60)

        CoordinatorClient(node).enable()

        Arcade.enableStepParticles()

        AdminCommand // init
    }

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        e.setQuitMessage(null)
    }
}
