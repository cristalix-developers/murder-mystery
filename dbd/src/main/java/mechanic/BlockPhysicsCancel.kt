package mechanic

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.player.PlayerToggleSprintEvent

object BlockPhysicsCancel : Listener {

    @EventHandler
    fun BlockPhysicsEvent.handle() {
        cancel = true
        isCancelled = true
    }

    @EventHandler
    fun PlayerToggleSprintEvent.handle() {
        player.isSprinting = false
    }

}