package me.func.murder

import me.func.murder.donate.impl.StepParticle
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 2 == 0) {
            Bukkit.getOnlinePlayers().forEach {
                val particle = app.getUser(it).stat.activeParticle
                if (particle != StepParticle.NONE)
                    it.world.spawnParticle(particle.type, it.location, 1)
            }
        }

        time = activeStatus.now(time) + 1
    }
}