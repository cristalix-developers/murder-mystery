package me.func.murder.util

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

    fun arrowEffect() {
        game.context.everyAfter(1, 1) {
            for (entity in game.map.world.getEntitiesByType<Arrow>().filter { it.shooter is Player }) {
                val user: User = game.userManager.getUser((entity.shooter as Player).uniqueId)

                if (user.stat.arrowParticle.particle != null)
                    game.map.world.spawnParticle(user.stat.arrowParticle.particle, entity.location, 1)
            }
        }
    }
}
