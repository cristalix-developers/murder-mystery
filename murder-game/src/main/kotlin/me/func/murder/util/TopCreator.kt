package me.func.murder.util

import me.func.murder.MurderGame
import me.func.murder.everyAfter
import me.func.murder.user.Stat
import org.bukkit.Location
import ru.cristalix.boards.bukkitapi.Boards
import ru.cristalix.core.account.IAccountService
import java.util.UUID

object TopCreator {

    fun create(
        game: MurderGame,
        location: Location,
        column: String,
        title: String,
        key: String,
        function: (Stat) -> String
    ) {
        val blocks = Boards.newBoard()
        blocks.addColumn("#", 18.0)
        blocks.addColumn("Игрок", 120.0)
        blocks.addColumn(column, 40.0)
        blocks.title = title
        blocks.location = location
        Boards.addBoard(blocks)

        game.context.everyAfter(20, 10 * 20) {
            game.kensuke.getLeaderboard(game.userManager, game.statScope, key, 10).thenAccept {
                blocks.clearContent()

                it.forEach { entry ->
                    if (entry.data.stat.lastSeenName == null) entry.data.stat.lastSeenName =
                        game.cristalix.getPlayer(UUID.fromString(entry.data.session.userId)).displayName

                    blocks.addContent(
                        UUID.fromString(entry.data.session.userId),
                        "" + entry.position,
                        entry.data.stat.lastSeenName,
                        "§d" + function(entry.data.stat)
                    )
                }

                blocks.updateContent()
            }
        }
    }
}
