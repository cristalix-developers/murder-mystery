package me.func.murder

import org.bukkit.scheduler.BukkitRunnable

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        time++
        val second = time / 20
        if (time % 20 == 0)
            time = activeStatus.now(second)
    }
}