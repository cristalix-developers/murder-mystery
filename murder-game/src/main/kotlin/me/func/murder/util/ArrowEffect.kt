package me.func.murder.util

import me.func.Arcade
import me.func.murder.MurderGame
import me.func.murder.everyAfter
import me.func.murder.getEntitiesByType
import me.func.murder.user.User
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
class ArrowEffect(private val game: MurderGame) {
    init {
        game.context.everyAfter(1, 1) {
            for (entity in game.map.world.getEntitiesByType<Arrow>().filter { it.shooter is Player }) {
                val user: User = game.userManager.getUser((entity.shooter as Player).uniqueId)

                val arrow = Arcade.getArcadeData(user.stat.id).arrowParticle

                if (arrow.type != null)
                    game.map.world.spawnParticle(arrow.type, entity.location, 1)
            }
        }
    }
}
