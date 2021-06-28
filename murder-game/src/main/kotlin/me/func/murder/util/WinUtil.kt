package me.func.murder.util

import me.func.murder.app
import me.func.murder.user.Role
import org.bukkit.Bukkit
import org.bukkit.GameMode

object WinUtil {

    fun check4win(): Boolean {
        // Получение всех активных ролей
        val activeRoles = Bukkit.getOnlinePlayers().asSequence()
            .filter { it.gameMode != GameMode.SPECTATOR }
            .map { app.getUser(it.uniqueId) }
            .filter { it.role != Role.NONE }
            .map { it.role }
            .distinctBy { it }.toList()
        // Если что то сломалось и игроков нет
        return if (activeRoles.isEmpty()) {
            return true
        } else if (activeRoles.size == 1 && activeRoles[0] == Role.MURDER) {
            // Если остался только один маньяк - победа маньяка
            return true
        } else if (activeRoles.size == 2 && Role.MURDER !in activeRoles) {
            // Если остался детектив или мирные - победа мирных
            return true
        } else false
    }

}