package me.func.commons.util

import me.func.commons.getByPlayer
import me.func.commons.user.User
import org.bukkit.Bukkit
import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.display.messages.RadioMessage

enum class Music(private val url: String) {

    LIGHT_OFF("https://implario.dev/murder/electro-break.mp3"),
    OUTLAST("https://implario.dev/murder/game.mp3"),
    FIELD("https://implario.dev/murder/field.mp3"),
    LOBBY("https://implario.dev/murder/waiting.mp3"),
    DBD_GAME("https://implario.dev/murder/dbd-game.mp3"),
    DBD_RUN("https://implario.dev/murder/run.mp3"),
    PORT("https://implario.dev/murder/port.mp3"),
    VILLAGER_WIN("https://implario.dev/murder/win.mp3")
    ;

    fun play(user: User) {
        MusicHelper.play(user, url)
    }

    fun playAll() {
        MusicHelper.playAll(url)
    }

}

object MusicHelper {
    fun play(user: User, url: String) {
        if (!user.stat.music)
            return
        stop(user)
        IDisplayService.get().sendRadio(user.player!!.uniqueId, RadioMessage(true, url))
    }

    fun playAll(url: String) {
        IDisplayService.get().sendRadio(Bukkit.getOnlinePlayers().filter {
            getByPlayer(it).stat.music
        }.map { it.uniqueId }, RadioMessage(true, url))
    }

    fun stop(user: User) {
        IDisplayService.get().sendRadio(user.player!!.uniqueId, RadioMessage(true, "null"))
    }

    fun stopAll() {
        IDisplayService.get().sendRadio(Bukkit.getOnlinePlayers().map { it.uniqueId }, RadioMessage(true, "null"))
    }
}