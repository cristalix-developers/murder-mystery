package me.func.murder

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.DefaultGameNode
import dev.implario.games5e.node.GameCreator
import dev.implario.games5e.node.linker.SessionBukkitLinker
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.murder.command.AdminCommand
import me.func.murder.user.Stat
import me.func.murder.user.User
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.BukkitPlatform
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import ru.cristalix.npcs.server.Npcs

lateinit var app: MurderApp

class MurderApp : JavaPlugin() {

    companion object {
        val LOBBY_SERVER: RealmId = RealmId.of("MURP-2")
    }

    private val core = CoreApi.get()
    private val statScope = Scope("murder-mystery", Stat::class.java)

    private lateinit var userManager: BukkitUserManager<User>
    private lateinit var kensuke: Kensuke

    lateinit var node: DefaultGameNode

    init {
        Platforms.set(PlatformDarkPaper())

        core.init(BukkitPlatform(Bukkit.getServer(), Bukkit.getLogger(), this))

        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        Npcs.init(this)
    }

    override fun onEnable() {
        app = this
        B.plugin = this

        userManager = BukkitUserManager(
            setOf(statScope),
            { session, context -> User(session, context.getData(statScope)) },
            { user, context -> context.store(statScope, user.stat) }
        ).apply { isOptional = true }

        kensuke = BukkitKensuke.setup(this).apply {
            addGlobalUserManager(userManager)
            globalRealm = "" // TODO: setup kensuke
        } // todo и я не поняла че делать с кенсуке и дбд

        node = DefaultGameNode()
        node.supportedImagePrefixes += setOf("murder-mystery", "murder-mystery-dbd")
        node.linker = SessionBukkitLinker.link(node)

        node.gameCreator = GameCreator { gameId, image, _ ->
            MurderGame(gameId, kensuke, userManager, statScope, image == "murder-mystery-dbd")
        }

        AdminCommand // init
    }

    /*override fun onEnable() {
        app = this
        Platforms.set(PlatformDarkPaper())

        val realmId = IRealmService.get().currentRealmInfo.realmId.id
        map = MapType.values().first { realmId % 10 == it.realmMod }

        MurderInstance(this, { getUser(it) }, { getUser(it) }, MapLoader.load(map.address), 16)

        realm.readableName = "Мардер #${realm.realmId.id} v.$version"
        realm.lobbyFallback = LOBBY_SERVER

        // Загрузка карты
        map.interactive.forEach { it.init() }
        map.loadDetails(worldMeta.world.entities.toTypedArray())

        // Создание раздатчика золота
        GoldManager(worldMeta.getLabels("gold").map { it.toCenterLocation() })
        // Регистрация менеджера выпавшего лука
        BowManager()

        // Запуск игрового таймера
        timer = Timer
        timer.runTaskTimer(this, 10, 1)

        // Создание контента для лобби
        TopManager()
        Npcs.init(this)
        CustomizationNPC()

        // Рисую эффект выстрела
        ArrowEffect().arrowEffect(this)
    }

    fun restart() {
        activeStatus = Status.STARTING
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Выключение сервера.") }

        // Полная перезагрузка если много игр наиграно
        if (games > GAMES_STREAK_RESTART)
            Bukkit.shutdown()
    }*/
}
