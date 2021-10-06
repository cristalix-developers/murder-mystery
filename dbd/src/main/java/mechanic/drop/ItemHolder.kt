package mechanic.drop

import killer
import mechanic.GadgetMechanic
import mechanic.GadgetMechanic.bandage
import mechanic.drop.ChestManager.fuel
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent

object ItemHolder : Listener {

    @EventHandler
    fun PlayerPickupItemEvent.handle() {
        if (killer?.player!! != player) {
            if (item.itemStack == fuel) {
                if (player.inventory.getItem(2) == null)
                    item.remove()
                player.inventory.setItem(2, fuel)
            } else if (item.itemStack == bandage) {
                if (player.inventory.getItem(3) == null)
                    item.remove()
                player.inventory.setItem(3, bandage)
            }
        }
        isCancelled = true
    }

}