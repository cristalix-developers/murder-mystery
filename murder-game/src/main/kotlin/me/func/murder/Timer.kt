package me.func.murder

import me.func.Arcade
import me.func.donate.impl.StepParticle
import org.bukkit.GameMode

class Timer(private val game: MurderGame) {
    var time = 0

    fun tick() {
        if (time % 2 == 0) {
            game.players.filter { it.gameMode != GameMode.SPECTATOR }.forEach {
                val particle = Arcade.getArcadeData(it).stepParticle
                if (particle != StepParticle.NONE) it.world.spawnParticle(
                    particle.type, it.location.clone().add(0.0, 0.2, 0.0), 1
                )
            }
        }

        time = game.activeStatus.now(time, game) + 1
    }
}
