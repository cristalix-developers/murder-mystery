package me.func.murder

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.murder.command.AdminCommand
import me.func.murder.interactive.InteractEvent
import me.func.murder.listener.*
import me.func.murder.lobbycontent.LobbyNPC
import me.func.murder.lobbycontent.LobbyTop
import me.func.murder.user.Stat
import me.func.murder.user.User
import me.func.murder.util.GoldManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*

const val GAMES_STREAK_RESTART = 10
lateinit var app: App
var activeStatus = Status.STARTING
var slots = 16
const val lobby = "HUB-1"
var games = 0

class App : JavaPlugin() {

    lateinit var kensuke: Kensuke
    val statScope = Scope("murder", Stat::class.java)
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

        // Регистрация сервисов
        val core = CoreApi.get()
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        // Конфигурация реалма
        val info = IRealmService.get().currentRealmInfo
        info.status = RealmStatus.WAITING_FOR_PLAYERS
        info.maxPlayers = slots
        info.readableName = "Мардер #${info.realmId.id}"
        info.groupName = "Мардер #${info.realmId.id}"

        // Подключение к сервису статистики
        kensuke = BukkitKensuke.setup(this)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = info.realmId.realmName
        userManager.isOptional = true

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
            InteractEvent()
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