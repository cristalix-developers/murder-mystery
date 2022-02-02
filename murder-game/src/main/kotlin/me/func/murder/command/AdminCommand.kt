package me.func.murder.command

import clepto.bukkit.B
import me.func.murder.MurderGame
import me.func.murder.app
import me.func.murder.getUser
import me.func.murder.user.User
import me.func.murder.util.MusicHelper
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting
import java.util.UUID

object AdminCommand {

    private val godSet = hashSetOf(
        UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd"),
        UUID.fromString("e7c13d3d-ac38-11e8-8374-1cb72caa35fd"),
        UUID.fromString("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd"),
        UUID.fromString("bf30a1df-85de-11e8-a6de-1cb72caa35fd")
    )

    init {
        regAdminCommandWithGame("give", "loot") cmd@{ user, game, args ->
            if (args.size < 2) {
                user.player!!.sendMessage(getNoArgsMessage("/give игрок кло-во", "/loot"))
                return@cmd false
            }

            game.userManager.getUser(Bukkit.getPlayer(args[0])).stat.lootbox += args[1].toInt()

            true
        }

        regAdminCommandWithGame("money") cmd@{ user, game, args ->
            if (args.size < 2) {
                user.player!!.sendMessage(getNoArgsMessage("/money игрок кол-во"))
                return@cmd false
            }

            game.userManager.getUser(Bukkit.getPlayer(args[0])).giveMoney(args[1].toInt())

            true
        }

        regAdminCommandWithGame("playall", "all") cmd@{ user, game, args ->
            if (args.isEmpty()) {
                user.player!!.sendMessage(getNoArgsMessage("/playall ссылкаНаМузыку", "/all"))
                return@cmd false
            }

            MusicHelper.playAll(game, args[0])

            true
        }

        regAdminCommandWithGame("play", "one") cmd@{ user, game, args ->
            if (args.size < 2) {
                user.player!!.sendMessage(getNoArgsMessage("/play игрок ссылкаНаМузыку", "/one"))
                return@cmd false
            }

            MusicHelper.play(game.userManager.getUser(Bukkit.getPlayer(args[0])), args[1])

            true
        }

        regAdminCommandWithGame("slot", "slots") cmd@{ user, game, args ->
            if (args.isEmpty()) {
                user.player!!.sendMessage(getNoArgsMessage("/slot колВоСлотов", "/slots"))
                return@cmd false
            }

            game.slots = args[0].toInt()

            true
        }
    }

    private fun regAdminCommandWithGame(
        command: String,
        vararg aliases: String,
        executor: (User, MurderGame, Array<String>) -> Boolean
    ) {
        B.regConsumerCommand({ player, args ->
            if (player.isOp || godSet.contains(player.uniqueId)) {
                val game = app.node.linker.getGameByPlayer(player) as MurderGame

                if (executor(game.userManager.getUser(player), game, args))
                    player.sendMessage(Formatting.fine("Успешно!"))
            } else {
                player.sendMessage(Formatting.error("Нет прав."))
            }
        }, command, *aliases)
    }

    private fun getNoArgsMessage(usage: String, vararg aliases: String): String {
        return Formatting.error(
            """Недостаточно аргументов
Использование:
    - $usage"""
        ).apply {
            if (aliases.isNotEmpty()) plus(
                """
Алиасы:
${aliases.joinToString("\n") { "    - $it" }}"""
            )
        }
    }
}
