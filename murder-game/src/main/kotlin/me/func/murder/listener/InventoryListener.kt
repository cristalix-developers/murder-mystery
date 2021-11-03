package me.func.murder.listener

import me.func.murder.Status
import me.func.murder.activeStatus
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType

object InventoryListener : Listener {
    @EventHandler
    fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.CHEST && activeStatus != Status.STARTING)
            isCancelled = true
    }
}