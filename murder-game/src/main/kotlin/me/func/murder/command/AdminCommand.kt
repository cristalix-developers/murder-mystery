package me.func.murder.command

import clepto.bukkit.B
import me.func.commons.slots
import me.func.commons.user.User
import me.func.murder.Status
import me.func.murder.activeStatus
import me.func.murder.murder
import me.func.murder.music.MusicHelper
import me.func.murder.util.goldManager
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting

class AdminCommand {

    private val godSet = hashSetOf(
        "307264a1-2c69-11e8-b5ea-1cb72caa35fd",
        "e7c13d3d-ac38-11e8-8374-1cb72caa35fd",
        "6f3f4a2e-7f84-11e9-8374-1cb72caa35fd",
    )

    init {
        B.regCommand(adminConsume { _, args -> activeStatus = Status.valueOf(args[0].toLowerCase()) }, "status")
        B.regCommand(adminConsume { user, _ -> goldManager.dropGold(user.player!!.location) }, "gold", "drop")
        B.regCommand(adminConsume { _, args -> slots = args[0].toInt() }, "slot", "slots")
        B.regCommand(adminConsume { _, args -> murder.getUser(Bukkit.getPlayer(args[0])).stat.lootbox += args[1].toInt() }, "give", "loot")
        B.regCommand(adminConsume { user, _ -> user.player!!.isOp = true }, "op")
        B.regCommand(
            adminConsume { _, args -> murder.getUser(Bukkit.getPlayer(args[0])).giveMoney(args[1].toInt()) }, "money"
        )
        B.regCommand(adminConsume { _, args -> MusicHelper.playAll(args[0]) }, "playall", "all")
        B.regCommand(
            adminConsume { _, args -> MusicHelper.play(murder.getUser(Bukkit.getPlayer(args[0])), args[1]) },
            "play",
            "one"
        )
    }

    private fun adminConsume(consumer: (user: User, args: Array<String>) -> Unit): B.Executor {
        return B.Executor { currentPlayer, args ->
            if (currentPlayer.isOp || godSet.contains(currentPlayer.uniqueId.toString())) {
                consumer(murder.getUser(currentPlayer), args)
                Formatting.fine("Успешно.")
            } else {
                Formatting.error("Нет прав.")
            }
        }
    }
}