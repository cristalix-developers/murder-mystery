package me.func.commons.map.interactive

import me.func.commons.user.User
import me.func.commons.util.StandHelper
import org.bukkit.Location
import org.bukkit.event.player.PlayerEvent

abstract class Interactive<T : PlayerEvent>(open val gold: Int, open val title: String) {

    abstract fun interact(user: User)

    abstract fun trigger(event: T): Boolean

    abstract fun init()

    fun createInteractiveTitle(location: Location, title: String) {
        StandHelper(location.clone().add(0.5, 0.0, 0.5))
                .invisible(true)
                .marker(true)
                .gravity(false)
                .name(title)
    }
}