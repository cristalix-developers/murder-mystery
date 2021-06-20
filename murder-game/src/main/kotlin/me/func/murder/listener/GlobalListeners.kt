package me.func.murder.listener

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class GlobalListeners : Listener {
    @EventHandler
    fun BlockPlaceEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun CraftItemEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEntityEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFadeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockSpreadEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockGrowEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFromToEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun HangingBreakByEntityEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockBurnEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun EntityExplodeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerAdvancementCriterionGrantEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun InventoryClickEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun FoodLevelChangeEvent.handle() {
        foodLevel = 20
    }
}