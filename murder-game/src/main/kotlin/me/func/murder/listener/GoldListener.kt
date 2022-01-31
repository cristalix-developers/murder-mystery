/*
package me.func.murder.listener

import dev.implario.bukkit.item.item
import me.func.commons.arrow
import me.func.commons.gold
import me.func.commons.user.Role
import me.func.murder.app
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerPickupArrowEvent

object GoldListener : Listener {

    private val bow = item {
        type = Material.BOW
        text("§eЛук")
        nbt("Unbreakable", 1)
    }

    @EventHandler
    fun PlayerPickupArrowEvent.handle() {
        arrow.remove()
        isCancelled = true
    }

    @EventHandler
    fun PlayerAttemptPickupItemEvent.handle() {
        if (item.itemStack.getType() != Material.GOLD_INGOT) {
            item.remove()
            isCancelled = true
            return
        }
        val itemStack = player.inventory.getItem(8)
        val user = app.getUser(player)
        user.giveMoney(1)
        if (itemStack != null) {
            player.inventory.addItem(gold)
            if (itemStack.getAmount() == 10 && user.role != Role.DETECTIVE) {
                player.inventory.remove(Material.GOLD_INGOT)
                player.inventory.setItem(if (user.role == Role.MURDER) 2 else 1, bow)
                if (player.inventory.contains(Material.ARROW))
                    player.inventory.addItem(arrow)
                else
                    player.inventory.setItem(20, arrow)
            }
        } else
            player.inventory.setItem(8, gold)
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        item.remove()
        isCancelled = true
    }

}*/
