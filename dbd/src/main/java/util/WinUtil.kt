package util

import activeStatus
import killer
import me.func.commons.user.Role
import murder
import org.bukkit.Bukkit
import timer

var winMessage = "§eЧто-то пошло не так..."

object WinUtil {

    fun check4win(): Boolean {
        val alive = Bukkit.getOnlinePlayers().map { murder.getUser(it) }.filter { it.role == Role.VICTIM }
        val out = Bukkit.getOnlinePlayers().map { murder.getUser(it) }.filter { it.out }

        return when {
            Bukkit.getOnlinePlayers().isEmpty() -> true
            alive.isEmpty() && out.isEmpty() && killer != null && killer!!.role == Role.MURDER -> {
                killer!!.stat.eventWins++
                winMessage = "§cМаньяк убил все живое..."
                true
            }
            (alive.isNotEmpty() || out.isNotEmpty()) && (killer == null || !killer!!.player!!.isOnline) -> {
                winMessage = "§dМаньяк вышел из игры..."
                true
            }
            alive.isEmpty() && out.isNotEmpty() -> {
                out.forEach { it.stat.eventWins++ }
                winMessage = "§aСпаслись: §f§l${out.joinToString{ it.player!!.name }}"
                true
            }
            activeStatus.lastSecond * 20 == timer.time && alive.isEmpty() && out.isEmpty() -> {
                killer!!.stat.eventWins++
                winMessage = "§cВремя вышло... Людей внутри больше не видели."
                true
            }
            activeStatus.lastSecond * 20 == timer.time && out.isNotEmpty() -> {
                out.forEach { it.stat.eventWins++ }
                winMessage = "§aЛюди спаслись, потерпевшие: §f§l${out.joinToString{ it.player!!.name }}"
                true
            }
            activeStatus.lastSecond * 20 == timer.time -> {
                killer!!.stat.eventWins++
                winMessage = "§cМаньяк убил все живое..."
                true
            }
            else -> false
        }
    }

}