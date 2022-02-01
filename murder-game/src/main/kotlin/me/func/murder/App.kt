package me.func.murder

import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.DefaultGameNode
import dev.implario.games5e.node.GameCreator
import dev.implario.games5e.node.linker.SessionBukkitLinker
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.commons.map.MapType
import me.func.commons.user.Stat
import me.func.commons.user.User
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
import java.util.UUID

const val GAMES_STREAK_RESTART = 6

lateinit var app: App
lateinit var map: MapType

val LOBBY_SERVER: RealmId = RealmId.of("MURP-2")

var activeStatus = Status.STARTING
var games = 0

class App : JavaPlugin() {

    private val core = CoreApi.get()
    private val statScope = Scope("squid-game", Stat::class.java)
    private val userManager = BukkitUserManager(
        setOf(statScope),
        { session, context -> User(session, context.getData(statScope)) },
        { user, context -> context.store(statScope, user.stat) }
    )

    lateinit var kensuke: Kensuke

    override fun onEnable() {
        app = this

        Platforms.set(PlatformDarkPaper())
        core.init(BukkitPlatform(Bukkit.getServer(), Bukkit.getLogger(), this))
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        Npcs.init(this)

        val node = DefaultGameNode()

        node.supportedImagePrefixes.add("murder-mystery")
        node.linker = SessionBukkitLinker.link(node)

        node.gameCreator = GameCreator { gameId, _, _ ->
            MurderGame(gameId)
        }

        kensuke = BukkitKensuke.setup(app)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = "" // TODO: setup kensuke
        userManager.isOptional = true
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

    fun getUser(player: Player): User {
        return getUser(player.uniqueId)
    }

    fun getUser(uuid: UUID): User {
        return userManager.getUser(uuid)
    }
}