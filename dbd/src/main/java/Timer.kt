import clepto.bukkit.B
import me.func.commons.mod.ModTransfer
import me.func.commons.slots
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

class Timer : BukkitRunnable() {
    var time = 0
    var playersBefore = 0

    override fun run() {
        // Обновление шкалы онлайна
        val players = Bukkit.getOnlinePlayers()
        if (playersBefore != players.size) {
            B.postpone(10) {
                players.forEach {
                    ModTransfer()
                        .integer(slots)
                        .integer(players.size)
                        .boolean(true)
                        .send("update-online", murder.getUser(it))
                }
            }
            playersBefore = players.size
        }

        time = activeStatus.now(time) + 1
    }
}