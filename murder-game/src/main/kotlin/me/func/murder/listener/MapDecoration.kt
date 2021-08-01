package me.func.murder.listener

import clepto.bukkit.B
import me.func.commons.worldMeta
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.spigotmc.event.entity.EntityDismountEvent
import java.util.*

class MapDecoration : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.handle() {
        if (clickedEntity.type != EntityType.ARMOR_STAND)
            return
        val stand = clickedEntity as ArmorStand
        if (stand.helmet == null || stand.helmet.getType() != Material.CLAY_BALL || stand.passengers.size > 0)
            return
        val nmsItem = CraftItemStack.asNMSCopy(stand.helmet)
        if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("murder", 8)) {
            val tag = nmsItem.tag.getString("murder")
            if (tag == "kreslo" || tag == "divan" || tag == "katalka") {
                stand.addPassenger(player)
            }
        }
    }

    @EventHandler
    fun EntityDismountEvent.handle() {
        val toTeleport = dismounted.passengers[0]
        B.postpone(1) {
            toTeleport.teleport(dismounted.location.clone().add(0.0, 4.0, 0.0))
        }
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (to.distanceSquared(from) < 0.4) {
            player.getNearbyEntities(0.5, 0.5, 0.5).filter { it.hasMetadata("friend") }
                .forEach {
                    worldMeta.world.getEntity(UUID.fromString(it.getMetadata("friend")[0].asString()))
                        .teleport(it.location.clone().subtract(0.0, 1.0, 0.0))
                }
        }
    }

}