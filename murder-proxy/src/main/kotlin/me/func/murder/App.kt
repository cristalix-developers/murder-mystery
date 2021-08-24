package me.func.murder

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.item.item
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.commons.*
import me.func.commons.content.CustomizationNPC
import me.func.commons.content.Lootbox
import me.func.commons.content.TopManager
import me.func.commons.listener.GlobalListeners
import me.func.commons.map.MapType
import me.func.commons.user.User
import me.func.commons.util.MapLoader
import me.func.commons.util.Music
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.display.data.DataDrawData
import ru.cristalix.core.display.data.StringDrawData
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.math.V2
import ru.cristalix.core.math.V3
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import ru.cristalix.core.render.VisibilityTarget
import ru.cristalix.core.render.WorldRenderData
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.util.*


lateinit var murder: App

class App : JavaPlugin(), Listener {

    private lateinit var cosmeticItem: ItemStack
    private lateinit var startItem: ItemStack
    private lateinit var backItem: ItemStack

    override fun onEnable() {
        B.plugin = this
        murder = this
        Platforms.set(PlatformDarkPaper())
        B.events(this, GlobalListeners(), Lootbox())
        MurderInstance(this, { getUser(it) }, { getUser(it) }, MapLoader.load("lobby"), 200)
        CoreApi.get().registerService(IRenderService::class.java, BukkitRenderService(getServer()))
        cosmeticItem = item {
            type = Material.CLAY_BALL
            text("§aПерсонаж")
            nbt("other", "clothes")
            nbt("click", "menu")
        }.build()
        startItem = item {
            type = Material.CLAY_BALL
            text("§bИграть")
            nbt("other", "guild_members")
            nbt("click", "next")
        }.build()
        backItem = item {
            type = Material.CLAY_BALL
            text("§cВыйти")
            nbt("other", "cancel")
            nbt("click", "leave")
        }.build()

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

        worldMeta.getLabels("play").forEach { npcLabel ->
            val npcArgs = npcLabel.tag.split(" ")
            val map = MapType.valueOf(npcArgs[0].toUpperCase())
            npcLabel.setYaw(npcArgs[1].toFloat())
            npcLabel.setPitch(npcArgs[2].toFloat())
            Npcs.spawn(
                Npc.builder()
                    .location(npcLabel.clone().add(0.5, -0.4, 0.5))
                    .name("§f >> §b§lИГРАТЬ §f<<")
                    .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                    .skinUrl("https://webdata.c7x.dev/textures/skin/${map.npcSkin}")
                    .skinDigest(map.npcSkin)
                    .type(EntityType.PLAYER)
                    .onClick {
                        if (fixDoubleClick != null && fixDoubleClick == it)
                            return@onClick
                        balancer.accept(it, map)
                        fixDoubleClick = it
                    }.build()
            )
            val textDataName = UUID.randomUUID().toString()
            IRenderService.get().createGlobalWorldRenderData(
                worldMeta.world.uid,
                textDataName,
                WorldRenderData.builder().visibilityTarget(VisibilityTarget.BLACKLIST).name(textDataName).dataDrawData(
                    DataDrawData.builder()
                        .strings(
                            listOf(
                                StringDrawData.builder().align(1).scale(2).position(V2(115.0, 0.0))
                                    .string("㗬㗬㗬")
                                    .build(),
                                StringDrawData.builder().align(1).scale(3).position(V2(115.0, 40.0))
                                    .string("§b" + map.title).build()
                            )
                        ).dimensions(V2(0.0, 0.0))
                        .scale(2.0)
                        .position(V3(npcLabel.x - 2, npcLabel.y + 4, npcLabel.z))
                        .rotation(0)
                        .build()
                ).build()
            )
            IRenderService.get().setRenderVisible(worldMeta.world.uid, textDataName, true)
        }

        // Команда выхода в хаб
        val hub = RealmId.of("HUB-1")
        B.regCommand({ player, _ ->
            Cristalix.transfer(listOf(player.uniqueId), hub)
            null
        }, "leave")
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
    fun BlockPhysicsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (item == null)
            return
        val nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("click", 8))
            player.performCommand(nmsItem.tag.getString("click"))
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        B.postpone(25) {
            val user = getUser(player)
            Music.LOBBY.play(user)
            user.sendPlayAgain("§aПопробовать!", MapType.OUTLAST)
        }

        player.inventory.setItem(0, startItem)
        player.inventory.setItem(4, cosmeticItem)
        player.inventory.setItem(8, backItem)

        if (Math.random() < 0.4)
            player.sendMessage(Formatting.fine("Рекомендуем сделать §eминимальную яркость §fи §bвключить звуки§f для полного погружения."))
    }
}