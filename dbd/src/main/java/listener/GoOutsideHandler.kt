package listener

import clepto.bukkit.B
import killer
import me.func.commons.user.Role
import me.func.commons.worldMeta
import murder
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

object GoOutsideHandler : Listener {

    private val winZone = worldMeta.getLabel("win")

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (to.distanceSquared(winZone) < winZone.tagInt * winZone.tagInt) {
            val user = murder.getUser(player)
            if (user.role == Role.VICTIM) {
                user.stat.wins++
                user.role = Role.NONE
                user.out = true
                user.giveMoney(3)

                player.gameMode = GameMode.SPECTATOR
                player.velocity = player.velocity.multiply(1.1).add(Vector(0.0, 1.6, 0.0))
                B.bc("  > §e${player.name} §aвыбрался наружу и убежал!")
            } else if (player == killer!!.player) {
                cancel = true
            }
        }
    }

}