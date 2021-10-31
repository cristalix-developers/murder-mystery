package mechanic.gate

import Status
import activeStatus
import clepto.bukkit.B
import clepto.bukkit.Cycle
import clepto.bukkit.world.Label
import jdk.nashorn.internal.objects.NativeArray.forEach
import killer
import me.func.commons.map.StandardsInteract.closeDoor
import me.func.commons.mod.ModHelper
import me.func.commons.util.StandHelper
import me.func.commons.worldMeta
import mechanic.GadgetMechanic
import murder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

data class Gate(
    val min: Location,
    val max: Location,
    val status: Location,
    val viewPoint: Label,
    var ticksResolved: Int,
    var hasPlayer: Boolean
) {

    val stand = StandHelper(status)
        .gravity(false)
        .invisible(true)
        .marker(false)
        .name("§cАктивируйте двигатели")
        .build()
    private val invisibility = PotionEffect(PotionEffectType.INVISIBILITY, 20 * 11, 1)
    private val weak = PotionEffect(PotionEffectType.WEAKNESS, 20 * 13, 250)

    init {
        val angle = viewPoint.tag.split(" ")
        viewPoint.yaw = angle[0].toFloat()
        viewPoint.pitch = angle[1].toFloat()
    }

    fun open() {
        Bukkit.getOnlinePlayers().forEach {
            it.addPotionEffect(invisibility)
            it.addPotionEffect(weak)
        }

        Bukkit.getOnlinePlayers().forEach {
            it.inventory.helmet = null
        }

        val blocks = mutableMapOf<Location, Pair<Material, Byte>>()

        val minCopy = min.clone()

        B.postpone(20 * 17) {
            Bukkit.getOnlinePlayers()
                .filter { killer?.player != it }
                .forEach { it.addPotionEffect(GadgetMechanic.blindness) }
        }

        Cycle.run(1, 10 * 17) { tick ->
            if (tick == 10 * 15 + 5) {
                stand.customName = "§aВрата открыты"
                ModHelper.sendGlobalTitle("§dВрата открыты!")
                repeat(3) {
                    B.postpone(13 + it * 2) {
                        Bukkit.getOnlinePlayers().map { player ->  murder.getUser(player) }.forEach { player -> player.player!!.teleport(player.tempLocation) }
                    }
                }
                closeDoor(blocks)
                Cycle.exit()
                return@run
            } else if (tick == 10 * 3) {
                worldMeta.world.strikeLightningEffect(max.clone().subtract(5.0, 5.0, 5.0))
            }

            if (tick > 3 && tick < 10 * 16) {
                Bukkit.getOnlinePlayers().forEach {
                    it.teleport(viewPoint)
                    it.removePotionEffect(PotionEffectType.BLINDNESS)
                }
            }

            if (tick > 3 * 10 && tick % 10 == 0) {

                for (y in 0..((max.y - min.y).toInt())) {
                    for (z in 0..((max.z - min.z).toInt())) {
                        minCopy.set(min.x + (tick - 3 * 10) / 10 - 1, min.y + y, min.z + z)
                        if (minCopy.block.type == Material.LOG_2)
                            return@run
                        blocks[minCopy.clone()] = minCopy.block.type to minCopy.block.data
                        minCopy.block.type = Material.AIR
                    }
                }
            }
        }
    }

    private fun closeDoor(blocks: MutableMap<Location, Pair<Material, Byte>>) {
        B.postpone(5 * 20) {
            if (activeStatus == Status.GAME) {
                closeDoor(blocks)
                return@postpone
            }
            hasPlayer = false
            ticksResolved = 0
            stand.customName = "§cАктивируйте двигатели"

            blocks.forEach { (location, type) -> location.block.setTypeAndDataFast(type.first.id, type.second) }
        }
    }
}
