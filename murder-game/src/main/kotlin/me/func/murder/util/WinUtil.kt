package me.func.murder.util

import me.func.commons.user.Role
import me.func.murder.activeStatus
import me.func.murder.murder
import me.func.murder.timer
import me.func.murder.winMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode

object WinUtil {

    fun check4win(): Boolean {
        // Если время вышло игроки победили
        if (activeStatus.lastSecond * 20 == timer.time) {
            winMessage = "§aВремя вышло! Мирные жители победили."
            return true
        }
        // Получение всех активных ролей
        val activeRoles = Bukkit.getOnlinePlayers().asSequence()
            .filter { it.gameMode != GameMode.SPECTATOR }
            .map { murder.getUser(it.uniqueId) }
            .filterNotNull()
            .filter { it.role != Role.NONE }
            .map { it.role }
            .distinctBy { it }.toList()
        // Если что то сломалось и игроков нет
        return if (activeRoles.isEmpty()) {
            winMessage = "§eЧто!?"
            return true
        } else if (activeRoles.size == 1 && activeRoles[0] == Role.MURDER) {
            // Если остался только один маньяк - победа маньяка
            winMessage = "§eМаньяк убил все живое!"
            return true
        } else if (activeRoles.size == 2 && Role.MURDER !in activeRoles) {
            // Если остался детектив или мирные - победа мирных
            winMessage = "§aМирные жители победили!"
            return true
        } else false
    }

}