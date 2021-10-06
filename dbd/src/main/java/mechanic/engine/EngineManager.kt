package mechanic.engine

import ENGINE_NEEDED
import activeStatus
import clepto.bukkit.B
import clepto.bukkit.Cycle
import killer
import me.func.commons.mod.ModHelper
import me.func.commons.util.Music
import me.func.commons.util.StandHelper
import me.func.commons.worldMeta
import mechanic.drop.ChestManager
import murder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import timer

object EngineManager : Listener {

    private val light: List<Location> = worldMeta.getLabels("light")
    private var engines: Map<Engine, Location> = worldMeta.getLabels("engine").associate {
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
        if (hasBlock() && player.gameMode != GameMode.SPECTATOR && player != killer?.player) {
            if (player.inventory.getItem(2) != ChestManager.fuel)
                return
            engines.filter { entry -> entry.key.percent < 100 && entry.key.location.distanceSquared(blockClicked.location) < 10 }
                .forEach { entry ->
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

                    ModHelper.sendTitle(murder.getUser(player), "${color + percent}%\n\n\n$percentLine §4⛽")
                    val haveEngines = enginesDone()
                    if (percent == 100) {
                        if (haveEngines < ENGINE_NEEDED) {
                            B.bc("  §l> §bИгрок §e${player.name} §bисправил движок! Осталось еще §e${ENGINE_NEEDED - haveEngines}")

                            Music.LIGHT_OFF.playAll()
                            B.postpone(20 * 10) { Music.DBD_GAME.playAll() }
                        } else if (haveEngines == ENGINE_NEEDED) {
                            ModHelper.sendGlobalTitle("§aДвигатели активированы\n§d✨✨✨")
                            B.bc("")
                            B.bc("  §l> §aИгрок §e${player.name} §aисправил движок! Все двигатели исправны!")
                            B.bc("")
                            showGates()
                            Bukkit.getOnlinePlayers().forEach { it.removePotionEffect(PotionEffectType.BLINDNESS) }
                        }
                    }
                }
        }
    }

    private val viewPoint = worldMeta.getLabel("gate")
    private val gates = worldMeta.getLabels("gates")

    init {
        val angle = viewPoint.tag.split(" ")
        viewPoint.yaw = angle[0].toFloat()
        viewPoint.pitch = angle[1].toFloat()
    }

    private fun showGates() {
        val invisibility = PotionEffect(PotionEffectType.INVISIBILITY, 20 * 11, 1)
        val weak = PotionEffect(PotionEffectType.WEAKNESS, 20 * 13, 250)

        Bukkit.getOnlinePlayers().forEach {
            it.addPotionEffect(invisibility)
            it.addPotionEffect(weak)
        }

        Bukkit.getOnlinePlayers().forEach {
            it.setBedSpawnLocation(it.location.clone(), true)
            it.inventory.helmet = null
        }
        val min = gates.minBy { it.x + it.y + it.z }!!
        val max = gates.maxBy { it.x + it.y + it.z }!!

        val blocks = mutableMapOf<Location, Pair<Material, Byte>>()

        val minCopy = min.clone()

        Cycle.run(1, 10 * 17) { tick ->
            if (tick == 10 * 15 + 5) {
                ModHelper.sendGlobalTitle("§dВорота открыты!")
                B.postpone(10) {
                    Bukkit.getOnlinePlayers().forEach { player -> player.teleport(player.bedSpawnLocation) }
                }
                B.postpone(activeStatus.lastSecond * 20 - timer.time - 20 * 20) {
                    blocks.forEach { (location, type) -> location.block.setTypeAndDataFast(type.first.id, type.second) }
                }
                Cycle.exit()
                return@run
            } else if (tick == 10 * 3) {
                worldMeta.world.strikeLightningEffect(max.clone().subtract(5.0, 5.0, 5.0))
            }

            Bukkit.getOnlinePlayers().forEach { it.teleport(viewPoint) }

            if (tick > 4 * 10 && tick % 10 == 0) {
                for (y in 0..((max.y - min.y).toInt())) {
                    for (z in 0..((max.z - min.z).toInt())) {
                        minCopy.set(min.x + (tick - 4 * 10) / 10 - 1, min.y + y, min.z + z)
                        blocks[minCopy.clone()] = minCopy.block.type to minCopy.block.data
                        minCopy.block.type = Material.AIR
                    }
                }
            }
        }
    }
}
