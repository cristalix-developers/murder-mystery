package mechanic.gate

import ENGINE_NEEDED
import Status
import activeStatus
import clepto.bukkit.B
import killer
import me.func.commons.worldMeta
import mechanic.engine.EngineManager
import murder
import org.bukkit.Bukkit
import org.bukkit.GameMode

object GateManager {

    private const val NEED_SECONDS = 30

    private val max = worldMeta.getLabels("gates", "max").toList()
    private val show = worldMeta.getLabels("show").toList()
    private val views = worldMeta.getLabels("view").toList()

    val gates = worldMeta.getLabels("gates", "min").map { label ->
        Gate(
            label,
            max.minByOrNull { label.distanceSquared(it) }!!,
            show.minByOrNull { label.distanceSquared(it) }!!.toCenterLocation().subtract(0.0, 1.5, 0.0),
            views.minByOrNull { label.distanceSquared(it) }!!,
            0,
            false
        )
    }

    init {
        B.repeat(2) {
            if (activeStatus != Status.GAME) {
                return@repeat
            } else if (EngineManager.enginesDone() >= ENGINE_NEEDED) {
                val players = Bukkit.getOnlinePlayers()
                    .filter { killer!!.player != it && it.gameMode != GameMode.SPECTATOR }

                gates.filter { it.ticksResolved < NEED_SECONDS * 20 }
                    .forEach { gate ->
                        val hasPlayer = players.any { it.location.distanceSquared(gate.status) < 10 }
                        val safeCombo = when (gate.ticksResolved / 20) {
                            in 0..9 -> 0
                            in 10..19 -> 10
                            else -> 20
                        }

                        if (gate.hasPlayer && hasPlayer) {
                            gate.ticksResolved += 2
                            gate.stand.customName =
                                "§eОткрытие через §f§l${safeCombo} §f㨼  +  §a§l${(gate.ticksResolved / 20) - safeCombo}"

                            if (gate.ticksResolved / 20 >= NEED_SECONDS) {
                                Bukkit.getOnlinePlayers()
                                    .map { player -> murder.getUser(player) }
                                    .forEach { it.tempLocation = it.player!!.location.clone() }
                                gate.open()
                            }
                        } else if (gate.hasPlayer) {
                            gate.ticksResolved = safeCombo * 20
                            gate.stand.customName = "§eОткрытие через §f§l${safeCombo} §f㨼"
                        } else {
                            gate.hasPlayer = true
                        }
                    }
            }
        }
    }

}