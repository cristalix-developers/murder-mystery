package me.func.murder.listener

import dev.implario.bukkit.item.item
import me.func.murder.app
import me.func.murder.user.Role
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
        val gold = player.inventory.getItem(8)
        if (gold != null) {
            player.inventory.addItem(toGive)
            val user = app.getUser(player)
            if (gold.getAmount() == 10 && user.role == Role.VILLAGER) {
                player.inventory.remove(Material.GOLD_INGOT)
                Role.DETECTIVE.start!!.invoke(user)
            }
        } else
            player.inventory.setItem(8, toGive)
        item.remove()
        isCancelled = true
    }

}