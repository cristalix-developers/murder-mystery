package me.func.murder

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.commons.getByPlayer
import me.func.commons.map.MapType
import me.func.commons.util.Music
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

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
            val user = getByPlayer(player)
            Music.LOBBY.play(user)
            user.sendPlayAgain("§aПопробовать!", MapType.OUTLAST)
        }

        player.inventory.setItem(0, startItem)
        player.inventory.setItem(4, cosmeticItem)
        player.inventory.setItem(8, backItem)

        if (Math.random() < 0.4)
            player.sendMessage(Formatting.fine("Рекомендуем сделать §eминимальную яркость §fи §bвключить звуки§f для полного погружения."))
    }

    @EventHandler
    fun EntityDamageEvent.handle() {
        cancelled = true
    }

    @EventHandler
    fun BlockPhysicsEvent.handle() {
        isCancelled = true
    }

}