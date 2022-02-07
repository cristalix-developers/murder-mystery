package me.func.murder

import me.func.murder.donate.impl.StepParticle
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable

class Timer(private val game: MurderGame) : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 2 == 0) {
            game.players.filter { it.gameMode != GameMode.SPECTATOR }
                .forEach {
                    val particle = game.userManager.getUser(it).stat.activeParticle
                    if (particle != StepParticle.NONE)
                        it.world.spawnParticle(particle.type, it.location.clone().add(0.0, 0.2, 0.0), 1)
                }
        }

        time = game.activeStatus.now(time, game) + 1
    }
}
