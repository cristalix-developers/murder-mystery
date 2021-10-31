package mechanic.engine

import ENGINE_NEEDED
import Status
import activeStatus
import clepto.bukkit.B
import clepto.bukkit.Cycle
import killer
import me.func.commons.map.StandardsInteract.closeDoor
import me.func.commons.mod.ModHelper
import me.func.commons.util.Music
import me.func.commons.util.StandHelper
import me.func.commons.worldMeta
import mechanic.GadgetMechanic
import mechanic.drop.ChestManager
import mechanic.gate.GateManager
import murder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object EngineManager : Listener {

    private val light: List<Location> = worldMeta.getLabels("light")
    var engines: Map<Engine, Location> = worldMeta.getLabels("engine").associate {
        Engine(
            it, 0, StandHelper(it.toCenterLocation().subtract(0.0, 1.2, 0.0))
                .gravity(false)
                .invisible(true)
                .name("§lДвигатель §e0%")
                .marker(true)
                .build()
        ) to light.minByOrNull { light -> light.distanceSquared(it) }!!.clone().subtract(0.0, 3.0, 0.0)
    }

    fun clearAll() {
        engines.forEach { (key, value) ->
            key.percent = 0
            key.stand?.customName = "§lДвигатель §e0%"
            value.block.setTypeAndDataFast(Material.IRON_BLOCK.id, 0.toByte())
            value.clone().add(0.0, 2.0, 0.0).block.setTypeAndDataFast(95, 14)
        }
    }

    fun enginesDone(): Int {
        return engines.keys.filter { engine -> engine.percent >= 100 }.size
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (hand != EquipmentSlot.OFF_HAND)
            return
        if (activeStatus == Status.GAME && hasBlock() && player.gameMode != GameMode.SPECTATOR && player != killer?.player) {
            engines.filter { entry -> entry.key.percent < 100 && entry.key.location.distanceSquared(blockClicked.location) < 14 }
                .forEach { entry ->
                    if (player.inventory.getItem(2) != ChestManager.fuel) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§cНайдите топливо в сундуке!"))
                        return
                    }

                    player.inventory.setItem(2, null)
                    entry.key.percent += minOf(25, 100 - entry.key.percent)

                    val percent = entry.key.percent

                    entry.key.stand?.customName = "§lДвигатель §e$percent%"
                    val glass = entry.value.clone().add(0.0, 2.0, 0.0).block

                    val color = when (percent) {
                        in 1..25 -> {
                            glass.setTypeAndDataFast(95, 14)
                            "§4"
                        }
                        in 25..50 -> {
                            glass.setTypeAndDataFast(95, 1)
                            "§6"
                        }
                        in 50..75 -> {
                            glass.setTypeAndDataFast(95, 4)
                            "§e"
                        }
                        else -> {
                            entry.value.block.setTypeAndDataFast(0, 0)
                            "§2"
                        }
                    }
                    var percentLine = color
                    repeat(percent * 8 / 100) { percentLine += "■" }
                    percentLine += "§0"
                    repeat(8 - percent * 8 / 100) { percentLine += "■" }

                    val user = murder.getUser(player)
                    user.fuel++
                    ModHelper.sendTitle(user, "${color + percent}%\n\n\n$percentLine §4⛽")

                    val haveEngines = enginesDone()

                    if (percent == 100) {
                        if (haveEngines < ENGINE_NEEDED) {
                            B.bc("  §l> §bИгрок §e${player.name} §bисправил движок! Осталось еще §e${ENGINE_NEEDED - haveEngines}")

                            player.world.strikeLightning(entry.key.location)
                            Music.LIGHT_OFF.playAll()
                            B.postpone(20 * 10) { Music.DBD_GAME.playAll() }
                        } else if (haveEngines == ENGINE_NEEDED) {
                            ModHelper.sendGlobalTitle("§aДвигатели активированы\n§d✨✨✨")
                            B.bc("")
                            B.bc("  §l> §aИгрок §e${player.name} §aисправил последний движок! §b§lОткрывайте врата")
                            B.bc("")
                            GateManager.gates.forEach { it.stand.customName = "§eПодойдите, чтобы открыть врата" }
                        }
                    }
                }
        }
    }
}
