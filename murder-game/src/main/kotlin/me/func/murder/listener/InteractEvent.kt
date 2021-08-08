package me.func.murder.listener

import me.func.commons.app
import me.func.commons.map.interactive.BlockInteract
import me.func.commons.mod.ModHelper
import me.func.murder.Status
import me.func.murder.activeStatus
import me.func.murder.murder
import me.func.murder.map
import me.func.murder.util.goldManager
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.cristalix.core.formatting.Formatting

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

    private val poisons = listOf(
        PotionEffect(PotionEffectType.BLINDNESS, 50, 1),
        PotionEffect(PotionEffectType.CONFUSION, 80, 1),
        PotionEffect(PotionEffectType.SLOW, 100, 1),
    )

    @EventHandler
    fun PlayerInteractAtEntityEvent.handle() {
        // Механика шприца
        if (hand == EquipmentSlot.OFF_HAND)
            return
        val item = player.itemInHand
        if (clickedEntity is CraftPlayer && item != null && item.getType() == Material.CLAY_BALL) {
            val nmsIem = CraftItemStack.asNMSCopy(item)
            if (nmsIem.hasTag() && nmsIem.tag.hasKeyOfType("interact", 8)) {
                player.itemInHand = null
                player.sendMessage(Formatting.error("Вы одурманили ${clickedEntity.name}"))
                (clickedEntity as CraftPlayer).addPotionEffects(poisons)
                ModHelper.sendTitle(murder.getUser(clickedEntity as CraftPlayer), "§cО нет! §aКиСлооТа")
            }
        }
    }
}