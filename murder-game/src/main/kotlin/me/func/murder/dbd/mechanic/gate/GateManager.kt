package me.func.murder.dbd.mechanic.gate

import me.func.murder.MurderGame
import me.func.murder.dbd.DbdStatus
import me.func.murder.getUser
import org.bukkit.GameMode

class GateManager(private val game: MurderGame) {

    companion object {
        private const val NEED_SECONDS = 30
    }

    private val max = game.map.getLabels("gates", "max").toList()
    private val show = game.map.getLabels("show").toList()
    private val views = game.map.getLabels("view").toList()

    val gates = game.map.getLabels("gates", "min").map { label ->
        Gate(
            label,
            max.minByOrNull { label.distanceSquared(it) }!!,
            show.minByOrNull { label.distanceSquared(it) }!!.toCenterLocation().subtract(0.0, 1.5, 0.0),
            views.minByOrNull { label.distanceSquared(it) }!!,
            0,
            false,
            game
        )
    }

    init {
        game.context.every(2) {
            if (game.activeDbdStatus != DbdStatus.GAME) {
                return@every
            } else if (game.engineManager.enginesDone() >= MurderGame.ENGINE_NEEDED) {
                val players = game.players.filter { game.killer!!.player != it && it.gameMode != GameMode.SPECTATOR }

                gates.filter { it.ticksResolved < NEED_SECONDS * 20 }.forEach { gate ->
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
                            game.players.map { player -> game.userManager.getUser(player) }
                                .forEach { it.tempLocation = it.player.location.clone() }
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
