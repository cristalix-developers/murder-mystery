package me.func.murder

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.commons.*
import me.func.commons.content.CustomizationNPC
import me.func.commons.content.Lootbox
import me.func.commons.content.TopManager
import me.func.commons.listener.GlobalListeners
import me.func.commons.listener.MapDecoration
import me.func.commons.user.User
import me.func.commons.util.MapLoader
import me.func.murder.listener.InteractEvent
import me.func.murder.listener.*
import me.func.commons.map.MapType
import me.func.murder.util.ArrowEffect
import me.func.murder.util.BowManager
import me.func.murder.util.GoldManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.npcs.server.Npcs
import java.util.*

const val GAMES_STREAK_RESTART = 6
lateinit var murder: App
lateinit var map: MapType
val LOBBY_SERVER: RealmId = RealmId.of("MURP-2")
var activeStatus = Status.STARTING
var games = 0

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        murder = this
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

        // Регистрация обработчиков событий
        B.events(
            DamageListener,
            ConnectionHandler,
            GlobalListeners,
            GoldListener,
            ChatListener,
            InteractEvent,
            Lootbox,
            InventoryListener,
            MapDecoration
        )

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
    }

    fun getUser(player: Player): User {
        return getUser(player.uniqueId)
    }

    fun getUser(uuid: UUID): User {
        return userManager.getUser(uuid)
    }
}