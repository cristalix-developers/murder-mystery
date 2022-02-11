package me.func.murder.dbd

import me.func.murder.MurderGame
import me.func.murder.getUser
import me.func.murder.mod.ModTransfer
import org.bukkit.scheduler.BukkitRunnable

class DbdTimer(private val game: MurderGame) {
    var time = 0
    var playersBefore = 0

    fun tick() {
        // Обновление шкалы онлайна
        val players = game.players
        if (playersBefore != players.size) {
            game.context.after(10) {
                players.forEach {
                    ModTransfer()
                        .integer(game.slots)
                        .integer(players.size)
                        .boolean(true)
                        .send("update-online", game.userManager.getUser(it))
                }
            }
            playersBefore = players.size
        }

        time = game.activeDbdStatus.now(time, game) + 1
    }
}
