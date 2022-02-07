package me.func.murder.util

import me.func.murder.MurderGame
import me.func.murder.user.Role
import org.bukkit.GameMode

class WinUtil(private val game: MurderGame) {

    fun check4win(): Boolean {
        // Если время вышло игроки победили
        if (game.activeDbdStatus.lastSecond * 20 == game.timer.time) {
            game.winMessage = "§aВремя вышло! Мирные жители победили."
            return true
        }
        // Получение всех активных ролей
        val activeRoles =
            game.players
                .asSequence()
                .filter { it.gameMode != GameMode.SPECTATOR }
                .map { game.userManager.getUser(it.uniqueId) }
                .filter { it.role != Role.NONE }
                .map { it.role }
                .distinctBy { it }
                .toList()

        // Если что то сломалось и игроков нет
        return if (activeRoles.isEmpty()) {
            game.winMessage = "§eЧто!?"
            return true
        } else if (activeRoles.size == 1 && activeRoles[0] == Role.MURDER) {
            // Если остался только один маньяк - победа маньяка
            game.winMessage = "§eМаньяк убил все живое!"
            return true
        } else if (activeRoles.size == 2 && Role.MURDER !in activeRoles) {
            // Если остался детектив или мирные - победа мирных
            game.winMessage = "§aМирные жители победили!"
            return true
        } else false
    }
}
