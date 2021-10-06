package me.func.commons

import clepto.bukkit.B
import clepto.cristalix.WorldMeta
import com.google.gson.GsonBuilder
import dev.implario.bukkit.item.item
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import me.func.commons.donate.DonateAdapter
import me.func.commons.donate.DonatePosition
import me.func.commons.map.MapType
import me.func.commons.user.Stat
import me.func.commons.user.User
import me.func.commons.util.ParticleHelper
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.datasync.EntityDataParameters
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
import java.awt.SystemColor.text
import java.util.*
import kotlin.properties.Delegates

lateinit var app: JavaPlugin
lateinit var getByPlayer: (Player) -> User
lateinit var getByUuid: (UUID) -> User
lateinit var kensuke: Kensuke
lateinit var worldMeta: WorldMeta
lateinit var realm: RealmInfo

var slots by Delegates.notNull<Int>()
val statScope = Scope("murder-new", Stat::class.java)
var userManager = BukkitUserManager(
    listOf(statScope),
    { session: KensukeSession, context -> User(session, context.getData(statScope)) },
    { user, context -> context.store(statScope, user.stat) }
)
val gold: ItemStack = item {
    type = Material.GOLD_INGOT
    text("§eЗолото\n\n§7Соберите §e10 штук§7,\n§7и получите §bлук§7!\n§7Или покупайте действия\n§7на карте.")
}.build()
val arrow: ItemStack = item {
    type = Material.ARROW
    text("§bСтрела")
}.build()
val light: ItemStack = item {
    type = Material.CLAY_BALL
    nbt("thief", "4")
    text("§6Фонарик §l§eПКМ")
}.build()

var version = 203

class MurderInstance(
    plugin: JavaPlugin,
    byPlayer: (Player) -> User,
    byUuid: (UUID) -> User,
    meta: WorldMeta,
    currentSlot: Int
) {

    init {
        app = plugin
        EntityDataParameters.register()
        worldMeta = meta

        // Регистрация сервисов
        val core = CoreApi.get()
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        // Конфигурация реалма
        slots = currentSlot
        realm = IRealmService.get().currentRealmInfo
        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.maxPlayers = currentSlot
        realm.groupName = "MurderMystery"

        // Подключение к сервису статистики
        kensuke = BukkitKensuke.setup(app)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = IRealmService.get().currentRealmInfo.realmId.realmName
        userManager.isOptional = true
        kensuke.gson = GsonBuilder()
            .registerTypeHierarchyAdapter(DonatePosition::class.java, DonateAdapter())
            .create()

        getByPlayer = byPlayer
        getByUuid = byUuid

        val lootbox = worldMeta.getLabel("lootbox").toCenterLocation().clone().subtract(0.0, 3.0, 0.0)
        var tick = 0
        B.repeat(1) {
            if (realm.status == RealmStatus.GAME_STARTED_RESTRICTED)
                return@repeat
            ParticleHelper.acceptTickBowDropped(lootbox, tick)
            tick++
        }

        val nextGame = PlayerBalancer("MUR", slots - 4)
        B.regCommand({ player: Player, args ->
            nextGame.accept(player, true)
            null
        }, "next")
    }

}