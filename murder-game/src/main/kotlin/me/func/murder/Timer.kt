package me.func.murder

import org.bukkit.scheduler.BukkitRunnable

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        time = activeStatus.now(time) + 1
    }
}