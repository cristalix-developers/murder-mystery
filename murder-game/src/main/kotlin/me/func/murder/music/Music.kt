package me.func.murder.music

import me.func.murder.user.User
import org.bukkit.Bukkit
import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.display.messages.RadioMessage

enum class Music(val url: String) {

    LIGHT_OFF("https://implario.dev/murder/electro-break.mp3"),
    OUTLAST("https://implario.dev/murder/game.mp3"),
    LOBBY("https://implario.dev/murder/waiting.mp3")
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
        stop(user)
        IDisplayService.get().sendRadio(user.player!!.uniqueId, RadioMessage(true, url))
    }

    fun playAll(url: String) {
        IDisplayService.get().sendRadio(Bukkit.getOnlinePlayers().map { it.uniqueId }, RadioMessage(true, url))
    }

    fun stop(user: User) {
        IDisplayService.get().sendRadio(user.player!!.uniqueId, RadioMessage(true, "null"))
    }

    fun stopAll() {
        IDisplayService.get().sendRadio(Bukkit.getOnlinePlayers().map { it.uniqueId }, RadioMessage(true, "null"))
    }
}