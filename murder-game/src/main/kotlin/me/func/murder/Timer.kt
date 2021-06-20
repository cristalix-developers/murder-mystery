package me.func.murder

import org.bukkit.scheduler.BukkitRunnable

class Timer : BukkitRunnable() {
    var time = 0
    val statuses = Status.values()

    override fun run() {
        time++
        val second = time / 20
        time = activeStatus.now(second)
    }
}