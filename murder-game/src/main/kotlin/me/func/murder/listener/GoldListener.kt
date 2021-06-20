package me.func.murder.listener

import dev.implario.bukkit.item.item
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent

class GoldListener : Listener {

    private val gold = item {
        type = Material.GOLD_INGOT
        text("§eЗолото")
    }.build()

    @EventHandler
    fun PlayerAttemptPickupItemEvent.handle() {
        val toGive = gold.clone()
        toGive.amount += player.inventory.getItem(8).amount
        player.inventory.setItem(8, toGive)
        item.remove()
    }

}