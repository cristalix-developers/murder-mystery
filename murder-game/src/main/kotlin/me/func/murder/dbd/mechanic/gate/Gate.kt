@file:Suppress("DEPRECATION")

package me.func.murder.dbd.mechanic.gate

import clepto.bukkit.Cycle
import me.func.murder.MurderGame
import me.func.murder.dbd.DbdStatus
import me.func.murder.dbd.mechanic.GadgetMechanic
import me.func.murder.getUser
import me.func.murder.util.StandHelper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

data class Gate(
    val min: Location,
    val max: Location,
    val status: Location,
    val viewPoint: dev.implario.bukkit.world.Label,
    var ticksResolved: Int,
    var hasPlayer: Boolean,
    private val game: MurderGame
) {

    val stand = StandHelper(status).apply {
        gravity(false)
        invisible(true)
        marker(false)
        name("§cАктивируйте двигатели")
    }.build()

    private val invisibility = PotionEffect(PotionEffectType.INVISIBILITY, 20 * 11, 1)
    private val weak = PotionEffect(PotionEffectType.WEAKNESS, 20 * 13, 250)

    init {
        val angle = viewPoint.tag.split(" ")
        viewPoint.yaw = angle[0].toFloat()
        viewPoint.pitch = angle[1].toFloat()
    }

    fun open() {
        game.players.forEach {
            it.addPotionEffect(invisibility)
            it.addPotionEffect(weak)
        }

        game.players.forEach {
            it.inventory.helmet = null
        }

        val blocks = mutableMapOf<Location, Pair<Material, Byte>>()

        val minCopy = min.clone()

        game.after(20 * 17) {
            game.players.filter { game.killer?.player != it }.forEach { it.addPotionEffect(GadgetMechanic.blindness) }
        }

        Cycle.run(1, 10 * 17) { tick ->
            if (tick == 10 * 15 + 5) {
                stand.customName = "§aВрата открыты"
                game.modHelper.sendGlobalTitle("§dВрата открыты!")
                repeat(3) {
                    game.after(13 + it * 2L) {
                        game.players.map { player ->
                            game.userManager.getUser(player)
                        }.forEach { player ->
                            player.player.teleport(player.tempLocation)
                        }
                    }
                }
                closeDoor(blocks)
                Cycle.exit()
                return@run
            } else if (tick == 10 * 3) {
                game.map.world.strikeLightningEffect(max.clone().subtract(5.0, 5.0, 5.0))
            }

            if (tick > 3 && tick < 10 * 16) {
                game.players.forEach {
                    it.teleport(viewPoint)
                    it.removePotionEffect(PotionEffectType.BLINDNESS)
                }
            }

            if (tick > 3 * 10 && tick % 10 == 0) {

                for (y in 0..((max.y - min.y).toInt())) {
                    for (z in 0..((max.z - min.z).toInt())) {
                        minCopy.set(min.x + (tick - 3 * 10) / 10 - 1, min.y + y, min.z + z)
                        if (minCopy.block.type == Material.LOG_2) return@run
                        blocks[minCopy.clone()] = minCopy.block.type to minCopy.block.data
                        minCopy.block.type = Material.AIR
                    }
                }
            }
        }
    }

    private fun closeDoor(blocks: MutableMap<Location, Pair<Material, Byte>>) {
        game.context.after(5 * 20) {
            if (game.activeDbdStatus == DbdStatus.GAME) {
                closeDoor(blocks)
                return@after
            }
            hasPlayer = false
            ticksResolved = 0
            stand.customName = "§cАктивируйте двигатели"

            blocks.forEach { (location, type) -> location.block.setTypeAndDataFast(type.first.id, type.second) }
        }
    }
}
