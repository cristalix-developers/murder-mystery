package me.func.murder.listener

import me.func.commons.map.interactive.BlockInteract
import me.func.murder.Status
import me.func.murder.activeStatus
import me.func.murder.murder
import me.func.murder.map
import me.func.murder.util.goldManager
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class InteractEvent : Listener {

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus != Status.GAME || hand == EquipmentSlot.OFF_HAND)
            return
        map.interactive.filterIsInstance<BlockInteract>()
            .filter { it.trigger(this) }
            .forEach {
                val user = murder.getUser(player)
                goldManager.take(user, it.gold) {
                    it.interact(user)
                    player.playSound(player.location, Sound.BLOCK_CLOTH_FALL, 1.1f, 1f)
                }
            }
    }
}