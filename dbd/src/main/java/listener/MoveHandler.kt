package listener

import Status
import activeStatus
import clepto.bukkit.B
import killer
import me.func.commons.mod.ModTransfer
import me.func.commons.user.Role
import me.func.commons.util.Music
import me.func.commons.worldMeta
import mechanic.GadgetMechanic
import mechanic.GadgetMechanic.traps
import mechanic.engine.EngineManager
import murder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

object MoveHandler : Listener {

    private val winZone = worldMeta.getLabels("win")

    @EventHandler
    fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.ENCHANTING)
            cancelled = true
    }

    @EventHandler
    fun PlayerBedEnterEvent.handle() {
        cancel = true
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (activeStatus == Status.GAME && player == killer!!.player && player.velocity.y > 0 && !player.isOnGround) {
            isCancelled = true
            cancel = true
        }

        if (to.blockX == from.blockX && to.blockY == from.blockY && to.blockZ == from.blockZ && player == killer?.player)
            return
        if (activeStatus == Status.GAME && player != killer!!.player && player.gameMode != GameMode.SPECTATOR) {
            traps.removeIf {
                if (it.location.distanceSquared(player.location) < 3.5) {
                    it.helmet = GadgetMechanic.closeTrap
                    player.addPotionEffect(GadgetMechanic.slowness)
                    Music.DBD_RUN.playAll()
                    B.postpone(20 * 5) { Music.DBD_GAME.playAll() }
                    return@removeIf true
                }
                return@removeIf false
            }
            winZone.filter { to.distanceSquared(it) < it.tagInt * it.tagInt }
                .forEach { _ ->
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
                        isCancelled = true
                        cancel = true
                    }
                }
            EngineManager.engines.filter { it.key.percent < 100 && it.key.location.distanceSquared(to) < 18}
                .forEach { (_, _) -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§eЗалейте топливо! §f§lПКМ §eпо двигателю")) }
        }
    }
}