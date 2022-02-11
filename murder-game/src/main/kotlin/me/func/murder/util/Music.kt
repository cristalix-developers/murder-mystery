package me.func.murder.util

import me.func.murder.MurderGame
import me.func.murder.getUser
import me.func.murder.user.User
import ru.cristalix.core.display.messages.RadioMessage

enum class Music(private val url: String) {

    LIGHT_OFF("https://implario.dev/murder/electro-break.mp3"),
    OUTLAST("https://implario.dev/murder/game.mp3"),
    FIELD("https://implario.dev/murder/field.mp3"),
    LOBBY("https://implario.dev/murder/waiting.mp3"),
    DBD_GAME("https://implario.dev/murder/dbd-game.mp3"),
    DBD_RUN("https://implario.dev/murder/dbd.mp3"),
    DBD_DEATH("https://implario.dev/murder/dbd-death.mp3"),
    PORT("https://implario.dev/murder/port.mp3"),
    VILLAGER_WIN("https://implario.dev/murder/win.mp3");

    fun play(user: User) = MusicHelper.play(user, url)

    fun playAll(game: MurderGame) = MusicHelper.playAll(game, url)
}

object MusicHelper {
    fun play(user: User?, url: String) {
        if (user == null)
            return
        if (user.stat.music == false) return // L43
        me.func.util.Music.sound(url, user.player!!)
    }

    fun playAll(game: MurderGame, url: String) {
        game.players.filter { game.userManager.getUser(it).stat.music }.forEach { me.func.util.Music.sound(url, it) }
    }

    fun stop(user: User?) {
        if (user == null)
            return
        if (user.player == null)
            return
        me.func.util.Music.stop(user.player!!)
    }
}
