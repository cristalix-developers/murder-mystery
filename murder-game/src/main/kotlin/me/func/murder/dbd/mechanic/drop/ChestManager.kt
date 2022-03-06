@file:Suppress("DEPRECATION")

package me.func.murder.dbd.mechanic.drop

import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import me.func.murder.MurderGame
import me.func.murder.dbd.DbdStatus
import me.func.murder.dbd.mechanic.GadgetMechanic
import me.func.murder.getUser
import me.func.murder.util.StandHelper
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_12_R1.Block
import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockAction
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class ChestManager(private val game: MurderGame) {

    companion object {
        val fuel: ItemStack = item {
            text("§l§eТопливо")
            nbt("brawl", "emz_weapeon")
            type = Material.CLAY_BALL
        }
    }

    private val chests = game.map.getLabels("chest").associate {
        it.block.getRelative(BlockFace.DOWN).location to LootChest(
            it,
            0,
            StandHelper(it.toCenterLocation().subtract(0.0, 0.6, 0.0)).gravity(false)
                .marker(true)
                .invisible(true)
                .name("§bОткрыть")
                .build()
        )
    }.toMutableMap()

    private val tickList = mutableListOf<Location>()

    init {
        game.context.every(5) {
            if (game.activeDbdStatus == DbdStatus.GAME) {
                tick()
            }
        }

        game.context.on<PlayerInteractEvent> { handle() }
        game.context.on<InventoryOpenEvent> { handle() }
    }

    private fun PlayerInteractEvent.handle() {
        if (player.gameMode == GameMode.SPECTATOR) return
        if (action == Action.RIGHT_CLICK_BLOCK && blockClicked?.type == Material.CHEST) isCancelled = true

        if (hasBlock() && blockClicked.type == Material.CHEST && chests[blockClicked.location]?.open == 0 && (game.killer?.player
                ?: false) != player
        ) {
            tickList.add(blockClicked.location)

            val location = blockClicked.location.clone().add(0.5, 1.0, 0.5)
            drop(location, fuel)
            if (Math.random() < 0.5) drop(location, GadgetMechanic.bandage)

            if (Math.random() < 0.4) {
                val energy = (Math.random() * 350).toInt()
                game.userManager.getUser(player).lightTicks += energy
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§l+${energy / 20} §fсек. света"))
            }
            val chest = chests[blockClicked.location]!!
            chest.open = 1
            chest.stand.customName = "§7Пусто"
        }
    }

    private fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.CHEST && game.activeDbdStatus == DbdStatus.GAME) {
            isCancelled = true
        }
    }

    private fun drop(location: Location, itemStack: ItemStack) {
        val item = location.world.dropItemNaturally(location.clone().add(0.5, 1.0, 0.5), itemStack)
        item.velocity = Vector(Math.random() - 0.5, 1.1, Math.random() - 0.5).multiply(0.1)
        item.customName = "§l§fx1 §f" + item.itemStack.itemMeta.displayName
        item.isCustomNameVisible = true
    }

    private fun tick() {
        // Безопасность превыше всего
        if (tickList.size > chests.size) tickList.clear()

        tickList.forEach {
            val position = BlockPosition(it.blockX, it.blockY, it.blockZ)
            val block = Block.getById(it.block.typeId)

            game.players.forEach { player ->
                (player as CraftPlayer).handle.playerConnection.sendPacket(
                    PacketPlayOutBlockAction(position, block, 1, 1)
                )
            }
        }
    }
}
