package me.func.murder

import clepto.bukkit.B
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.implario.bukkit.platform.Platforms
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.murder.command.AdminCommand
import me.func.murder.donate.DonateAdapter
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.impl.Corpse
import me.func.murder.interactive.InteractEvent
import me.func.murder.listener.*
import me.func.murder.lobbycontent.LobbyNPC
import me.func.murder.lobbycontent.LobbyTop
import me.func.murder.lobbycontent.Lootbox
import me.func.murder.user.Stat
import me.func.murder.user.User
import me.func.murder.util.BowManager
import me.func.murder.util.GoldManager
import me.func.murder.util.droppedBowManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.GlobalSerializers
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*

const val GAMES_STREAK_RESTART = 10
const val lobby = "HUB-1"
lateinit var app: App
lateinit var realm: RealmInfo
var activeStatus = Status.STARTING
var slots = 16
var games = 0

class App : JavaPlugin() {

    lateinit var kensuke: Kensuke
    val statScope = Scope("murder-new", Stat::class.java)
    var userManager = BukkitUserManager(
        listOf(statScope),
        { session: KensukeSession, context -> User(session, context.getData(statScope)) },
        { user, context -> context.store(statScope, user.stat) }
    )

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())

        // Загрузка карты
        MapLoader()
        map.interactive.forEach { it.init() }

        // Создание раздатчика золота
        GoldManager(worldMeta.getLabels("gold").map { it.toCenterLocation() })
        // Регистрация менеджера выпавшего лука
        BowManager()

        // Регистрация сервисов
        val core = CoreApi.get()
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        // Конфигурация реалма
        realm = IRealmService.get().currentRealmInfo
        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.maxPlayers = slots
        realm.readableName = "Мардер #${realm.realmId.id}"
        realm.groupName = "Мардер #${realm.realmId.id}"

        // Подключение к сервису статистики
        kensuke = BukkitKensuke.setup(this)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = realm.realmId.realmName
        userManager.isOptional = true
        kensuke.gson = GsonBuilder()
            .registerTypeHierarchyAdapter(DonatePosition::class.java, DonateAdapter())
            .create()

        // Запуск игрового таймера
        timer = Timer()
        timer.runTaskTimer(this, 10, 1)

        // Регистрация обработчиков событий
        B.events(
            DamageListener(),
            ConnectionHandler(),
            GlobalListeners(),
            GoldListener(),
            ChatListener(),
            InteractEvent(),
            Lootbox()
        )

        // Регистрация админ команд
        AdminCommand()

        // Создание контента для лобби
        LobbyTop()
        LobbyNPC()
    }

    fun restart() {
        // Кик всех игроков с сервера
        clepto.cristalix.Cristalix.transfer(
            Bukkit.getOnlinePlayers().map { it.uniqueId },
            ru.cristalix.core.realm.RealmId.of(lobby)
        )
        // Очистка мусорных сущностей
        worldMeta.world.entities.filter { it.hasMetadata("trash") }
            .forEach { it.remove() }
        droppedBowManager.clear()
        activeStatus = Status.STARTING

        // Полная перезагрузка если много игр наиграно
        if (games > GAMES_STREAK_RESTART)
            Bukkit.shutdown()
    }

    fun getUser(player: Player): User {
        return getUser(player.uniqueId)
    }

    fun getUser(uuid: UUID): User {
        return userManager.getUser(uuid)
    }
}