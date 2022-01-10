package me.func.murder

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.item.item
import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.packets.PacketOk
import dev.implario.games5e.packets.PacketQueueEnter
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import io.netty.buffer.Unpooled
import me.func.commons.*
import me.func.commons.content.CustomizationNPC
import me.func.commons.content.Lootbox
import me.func.commons.content.TopManager
import me.func.commons.listener.GlobalListeners
import me.func.commons.mod.ModTransfer
import me.func.commons.user.User
import me.func.commons.util.MapLoader
import me.func.commons.util.StandHelper
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.display.data.DataDrawData
import ru.cristalix.core.display.data.StringDrawData
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.lib.Futures
import ru.cristalix.core.math.V2
import ru.cristalix.core.math.V3
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import ru.cristalix.core.render.VisibilityTarget
import ru.cristalix.core.render.WorldRenderData
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.awt.SystemColor.text
import java.util.*
import java.util.concurrent.TimeUnit


lateinit var murder: App

class App : JavaPlugin() {

    val client = CoordinatorClient(NoopGameNode())
    private var squidSlot = 80

    private val online = mutableMapOf<GameType, ArmorStand>()

    override fun onEnable() {
        B.plugin = this
        murder = this
        Platforms.set(PlatformDarkPaper())

        B.events(GlobalListeners, Lootbox, LobbyHandler)

        MurderInstance(this, { getUser(it) }, { getUser(it) }, MapLoader.load("lobby"), 270)

        CoreApi.get().registerService(IRenderService::class.java, BukkitRenderService(getServer()))

        // Конфигурация реалма
        realm.isLobbyServer = true
        realm.readableName = "MurderMystery Lobby"
        realm.servicedServers = arrayOf("MURP", *GameType.values().map { it.name }.toTypedArray())
        realm.extraSlots = 10

        B.repeat(10) {
            GameType.values().mapNotNull { it.queue }.forEach { uuid ->
                val count = client.queueOnline.getOrDefault(UUID.fromString(uuid), null) ?: return@forEach

                Bukkit.getOnlinePlayers().forEach {
                    try {
                        val serializer = PacketDataSerializer(Unpooled.buffer())
                        serializer.writeInt(count)
                        (it as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutCustomPayload("queue:online", serializer))
                    } catch (exception: Exception) {}
                }
            }
            online.forEach { (game, stand) ->
                val online = IRealmService.get().getOnlineOnRealms(game.name)
                stand.customName = "§b${online} игроков в игре"
            }
        }

        // Создание контента для лобби
        TopManager()
        Npcs.init(app)
        CustomizationNPC()
        // NPC поиска игры
        val balancer = PlayerBalancer()
        var fixDoubleClick: Player? = null

        client.listenQueues()
        client.enable()

        worldMeta.getLabels("play").forEach { npcLabel ->
            val npcArgs = npcLabel.tag.split(" ")

            if (npcArgs[0].toUpperCase() == "AMN")
                return@forEach

            val type = GameType.valueOf(npcArgs[0].toUpperCase())
            npcLabel.setYaw(npcArgs[1].toFloat())
            npcLabel.setPitch(npcArgs[2].toFloat())

            online[type] = StandHelper(npcLabel.clone().add(0.5, 1.85, 0.5))
                .gravity(false)
                .invisible(true)
                .marker(true)
                .name("99 игроков в игре")
                .build()

            Npcs.spawn(
                Npc.builder()
                    .location(npcLabel.clone().add(0.5, -0.5, 0.5))
                    .name("§e§lНАЖМИТЕ ЧТОБЫ ИГРАТЬ")
                    .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                    .skinUrl(type.skin)
                    .skinDigest(type.skin.substring(type.skin.lastIndexOf("/") + 1))
                    .type(EntityType.PLAYER)
                    .onClick {
                        if (fixDoubleClick != null && fixDoubleClick == it)
                            return@onClick
                        fixDoubleClick = it

                        if (type.queue != null) {
                            Futures.timeout(IPartyService.get().getPartyByMember(it.uniqueId), 3, TimeUnit.SECONDS).whenComplete { group, throwable ->
                                if (throwable == null) {
                                    if (!group.map { party -> party.leader == it.uniqueId }.orElse(true)) {
                                        it.sendMessage(Formatting.error("Вы не лидер пати."))
                                        return@whenComplete
                                    }
                                }
                                Futures.timeout(
                                    client.client.send(
                                        PacketQueueEnter(
                                            UUID.fromString(type.queue),
                                            if (throwable == null && group.isPresent) group.get().members.toList() else listOf(it.uniqueId),
                                            true, true, HashMap()
                                        )
                                    ).awaitFuture(PacketOk::class.java), 2, TimeUnit.SECONDS
                                ).whenComplete { _, error ->
                                    if (error != null) {
                                        error.printStackTrace()
                                        it.sendMessage(Formatting.error("Ошибка: ${error::class.simpleName} ${error.message}"))
                                    } else {
                                        it.sendMessage(Formatting.fine("Вы добавлены в очередь!"))
                                        ModTransfer()
                                            .string(type.name.toLowerCase())
                                            .integer(type.slots)
                                            .send("queue:show", getUser(it))
                                    }
                                }
                            }
                            return@onClick
                        } else {
                            balancer.accept(it, type.name)
                        }
                    }.build()
            )
            val working = IRealmService.get().hasAnyRealmsOfType(type.name)
            val prefix = (if (working) "§a" else "§c") + "◉ " + (if (working) "" else "Закрыто ")
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
                                StringDrawData.builder().align(1).scale(2).position(V2(115.0, 40.0))
                                    .string(prefix + type.string).build()
                            )
                        ).dimensions(V2(0.0, 0.0))
                        .scale(2.0)
                        .position(V3(npcLabel.x - 2, npcLabel.y + 4.1, npcLabel.z))
                        .rotation(0)
                        .build()
                ).build()
            )
            IRenderService.get().setRenderVisible(worldMeta.world.uid, textDataName, true)
        }

        // Команда выхода в хаб
        val hub = RealmId.of("HUB-2")
        B.regCommand({ player, _ ->
            Cristalix.transfer(listOf(player.uniqueId), hub)
            null
        }, "leave")

        B.regCommand({ player, arg ->
            if (player.isOp)
                squidSlot = arg[0].toInt()
            null
        }, "squid")

        B.regCommand({ player, arg ->
            Bukkit.getOnlinePlayers().forEach {
                player.hidePlayer(murder, it)
            }
            Formatting.fine("Игроки скрыты!")
        }, "hide")
    }

    fun getUser(player: Player): User {
        return getUser(player.uniqueId)
    }

    private fun getUser(uuid: UUID): User {
        return userManager.getUser(uuid)
    }
}