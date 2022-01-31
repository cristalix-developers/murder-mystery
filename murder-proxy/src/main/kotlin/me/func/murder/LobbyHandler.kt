package me.func.murder

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import dev.implario.games5e.packets.PacketQueueLeave
import io.netty.buffer.Unpooled
import me.func.commons.app
import me.func.commons.getByPlayer
import me.func.commons.getByUuid
import me.func.commons.map.MapType
import me.func.commons.mod.ModTransfer
import me.func.commons.util.Music
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import java.util.*

object LobbyHandler : Listener {

    private var cosmeticItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }.build()
    private var startItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§bИграть")
        nbt("other", "guild_members")
        nbt("click", "next")
    }.build()
    private var backItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }.build()
    private var hideItem: ItemStack = item {
        type = Material.EMERALD
        text("§bСкрыть игроков")
        nbt("click", "hide")
    }.build()

    init {
        Bukkit.getMessenger().registerIncomingPluginChannel(murder, "queue:leave") { _, player, _ ->
            leave(player.uniqueId)
            val serializer = PacketDataSerializer(Unpooled.buffer())
            serializer.writeInt(0)
            (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutCustomPayload("queue:hide", serializer))
        }
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
        if (hasBlock() && blockClicked.type != Material.ENDER_CHEST)
            isCancelled = true
        if (item == null)
            return
        val nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("click", 8))
            player.performCommand(nmsItem.tag.getString("click"))
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        B.postpone(25) {
            val user = getByPlayer(player)
            Music.LOBBY.play(user)
            user.sendPlayAgain("§aПопробовать!", MapType.OUTLAST)
        }

        if (Bukkit.getOnlinePlayers().size > 50) {
            Bukkit.getOnlinePlayers().forEach {
                if (Math.random() < 0.8) {
                    it.hidePlayer(murder, player)
                    player.hidePlayer(murder, it)
                }
            }
        }

        player.gameMode = GameMode.ADVENTURE
        player.inventory.setItem(0, startItem)
        player.inventory.setItem(2, hideItem)
        player.inventory.setItem(4, cosmeticItem)
        player.inventory.setItem(8, backItem)

        if (Math.random() < 0.3)
            player.sendMessage(Formatting.fine("Рекомендуем сделать §eминимальную яркость §fи §bвключить звуки§f для полного погружения."))
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        leave(player.uniqueId)
    }

    private fun leave(uuid: UUID) {
        murder.client.client.send(
            PacketQueueLeave(Collections.singletonList(uuid))
        )
    }
}