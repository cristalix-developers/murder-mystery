package util

import activeStatus
import killer
import me.func.commons.user.Role
import murder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import timer

var winMessage = "§eЧто-то пошло не так..."

object WinUtil {

    fun check4win(): Boolean {
        val alive = Bukkit.getOnlinePlayers().map { murder.getUser(it) }.filter { it.role == Role.VICTIM }
        val out = Bukkit.getOnlinePlayers().map { murder.getUser(it) }.filter { it.out }

        return when {
            Bukkit.getOnlinePlayers().isEmpty() -> true
            alive.isEmpty() && out.isEmpty() && killer != null && killer!!.role == Role.MURDER -> {
                killer!!.stat.wins++
                winMessage = "§cМаньяк убил все живое..."
                true
            }
            (alive.isNotEmpty() || out.isNotEmpty()) && (killer == null || !killer!!.player!!.isOnline) -> {
                winMessage = "§dМаньяк вышел из игры..."
                true
            }
            alive.isEmpty() && out.isNotEmpty() -> {
                winMessage = "§aВсе спаслись: §f§l${out.joinToString{ it.player!!.name }}"
                true
            }
            activeStatus.lastSecond * 20 == timer.time && alive.isEmpty() && out.isEmpty() -> {
                winMessage = "§cВремя вышло... Людей внутри больше не видели."
                true
            }
            activeStatus.lastSecond * 20 == timer.time && out.isNotEmpty() -> {
                winMessage = "§aЛюди спаслись, потерпевшие: §f§l${out.joinToString{ it.player!!.name }}"
                true
            }
            else -> false
        }
    }

}