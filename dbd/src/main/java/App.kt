import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import listener.DeathHandler
import listener.JoinListener
import listener.MoveHandler
import me.func.commons.MurderInstance
import me.func.commons.content.CustomizationNPC
import me.func.commons.content.Lootbox
import me.func.commons.content.TopManager
import me.func.commons.listener.GlobalListeners
import me.func.commons.listener.MapDecoration
import me.func.commons.map.MapType
import me.func.commons.realm
import me.func.commons.user.User
import me.func.commons.userManager
import me.func.commons.util.MapLoader
import me.func.commons.worldMeta
import mechanic.BlockPhysicsCancel
import mechanic.GadgetMechanic
import mechanic.GadgetMechanic.clearTraps
import mechanic.drop.ChestManager
import mechanic.drop.ItemHolder
import mechanic.engine.EngineManager
import mechanic.gate.GateManager
import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.npcs.server.Npcs
import java.util.*

const val GAMES_STREAK_RESTART = 14
const val ENGINE_NEEDED = 7
lateinit var murder: App
lateinit var map: MapType
var killer: User? = null
val LOBBY_SERVER: RealmId = RealmId.of("MURP-2")
var activeStatus = Status.STARTING
var games = 0

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        murder = this
        Platforms.set(PlatformDarkPaper())

        map = if (IRealmService.get().currentRealmInfo.realmId.id < 500) MapType.DBD else MapType.DBD2

        MurderInstance(this, { getUser(it) }, { getUser(it) }, MapLoader.load(map.address), 6)
        realm.readableName = "DBD${realm.realmId.id} v.1.0.2"
        realm.lobbyFallback = LOBBY_SERVER

        // Запуск игрового таймера
        timer = Timer()
        timer.runTaskTimer(this, 10, 1)

        // Регистрация обработчиков событий
        B.events(
            GlobalListeners,
            MapDecoration,
            JoinListener,
            DeathHandler,
            Lootbox,
            BlockPhysicsCancel,
            EngineManager,
            ChestManager,
            ItemHolder,
            GadgetMechanic,
            MoveHandler,
        )

        // Загрузка врат
        GateManager
        // Загрузка размеров стендов
        map.loadDetails(worldMeta.world.entities.toTypedArray())

        // Создание контента для лобби
        TopManager()
        Npcs.init(this)
        CustomizationNPC()
    }

    fun restart() {
        activeStatus = Status.STARTING
        ChestManager.hideAll()
        clearTraps()
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Выключение сервера.") }
        worldMeta.world.livingEntities.filter { it is Item }.forEach { it.remove() }

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