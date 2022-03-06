package me.func.murder

import dev.implario.bukkit.item.item
import dev.implario.games5e.node.Game
import dev.implario.games5e.sdk.cristalix.Cristalix
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.UserManager
import me.func.Arcade
import me.func.murder.content.TopManager
import me.func.murder.dbd.DbdStatus
import me.func.murder.dbd.DbdTimer
import me.func.murder.dbd.mechanic.GadgetMechanic
import me.func.murder.dbd.mechanic.engine.EngineManager
import me.func.murder.dbd.mechanic.gate.GateManager
import me.func.murder.dbd.util.DbdWinUtil
import me.func.murder.map.Gurney
import me.func.murder.map.MapType
import me.func.murder.map.StandardsInteract
import me.func.murder.mod.ModHelper
import me.func.murder.user.Stat
import me.func.murder.user.User
import me.func.murder.util.ArrowEffect
import me.func.murder.util.BowManager
import me.func.murder.util.GoldManager
import me.func.murder.util.ParticleHelper
import me.func.murder.util.WinUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.TransferService
import java.util.UUID

/**
 * Created by Kamillaova on 2022.01.30.
 */
data class MurderSettings(val teams: List<List<UUID>>)

class MurderGame(
    gameId: UUID,
    settings: MurderSettings,
    val kensuke: Kensuke,
    val userManager: UserManager<User>,
    val statScope: Scope<Stat>,
    val dbd: Boolean
) : Game(gameId) {

    companion object {
        const val ENGINE_NEEDED = 7 // dbd

        val gold: ItemStack = item {
            type = Material.GOLD_INGOT
            text("§eЗолото\n\n§7Соберите §e10 штук§7,\n§7и получите §bлук§7!\n§7Или покупайте действия\n§7на карте.")
        }

        val arrow: ItemStack = item {
            type = Material.ARROW
            text("§bСтрела")
        }

        val light: ItemStack = item {
            type = Material.CLAY_BALL
            nbt("thief", "4")
            text("§6Фонарик §l§eПКМ")
        }
    }

    var murderName: String? = null
    var detectiveName: String? = null
    var winMessage: String? = null
    var heroName: String? = null

    var killer: User? = null

    var activeStatus: Status = Status.STARTING

    var activeDbdStatus: DbdStatus = DbdStatus.STARTING

    var minPlayers = if (dbd) 5 else 10

    var slots = if (dbd) minPlayers else 16

    val modHelper = ModHelper(this)
    val goldManager = GoldManager(this)
    val particleHelper = ParticleHelper(this)
    val bowManager = BowManager(this)
    val winUtil = WinUtil(this)
    val gurney = Gurney(this)
    val standardsInteract = StandardsInteract(this)
    val timer: Timer = Timer(this)

    lateinit var engineManager: EngineManager
        private set
    lateinit var gateManager: GateManager
        private set
    lateinit var dbdWinUtil: DbdWinUtil
        private set
    lateinit var dbdTimer: DbdTimer
        private set
    lateinit var gadgetMechanic: GadgetMechanic
        private set

    val mapType: MapType = if (dbd) arrayOf(MapType.DBD, MapType.DBD2).random()
    else arrayOf(MapType.FIELD, MapType.OUTLAST, MapType.PORT).random()

    val map: WorldMeta = MapLoader.load(this, "Murder", mapType.address).apply {
        after(40) {
            world.setGameRuleValue("doDaylightCycle", "false")
            world.fullTime = 18000
        }
    }

    val spawn: Location = run {
        val dot = map.getLabel("spawn")
        val args = dot.tag.split(" ")
        if (args.size > 1) {
            dot.setYaw(args[0].toFloat())
            dot.setPitch(args[1].toFloat())
        }

        dot.add(0.5, 0.0, 0.5)
    }

    val cristalix: Cristalix = Cristalix.connectToCristalix(
        this, //
        if (dbd) "DBD" else "MUR", //
        if (dbd) "Dead By Daylight" else "MurderMystery"
    )

    private val transferService = TransferService(cristalix.client)

    init {
        cristalix.setRealmInfoBuilder { it.lobbyFallback(Arcade.getLobbyRealm()) }
        cristalix.updateRealmInfo()

        GameListeners(this, dbd)

        if (dbd) {
            engineManager = EngineManager(this)
            gateManager = GateManager(this)
            dbdWinUtil = DbdWinUtil(this)
            gadgetMechanic = GadgetMechanic(this)
            dbdTimer = DbdTimer(this).apply { context.everyAfter(10, 1) { dbdTimer.tick() } }
        } else {
            mapType.interactive.forEach { it.init(this) }
            ArrowEffect(this)
            TopManager(this)

            after(10) {
                mapType.loadDetails(map.world.entities.toTypedArray())
                every(1) { timer.tick() }
            }
        }

        transferService.transferBatch(settings.teams.flatten(), cristalix.realmId)
    }

    fun stopGame(transfer: Boolean = true) {
        if (transfer) transferService.transferBatch(players.map { it.uniqueId }, Arcade.getLobbyRealm())

        after(10) {
            isTerminated = true
            Bukkit.unloadWorld(map.world, false)

            unregisterAll()
        }
    }

    override fun acceptPlayer(e: AsyncPlayerPreLoginEvent) = players.size <= slots

    override fun getSpawnLocation(uuid: UUID): Location = spawn
}
