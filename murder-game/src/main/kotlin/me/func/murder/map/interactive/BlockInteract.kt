package me.func.murder.map.interactive

import me.func.murder.MurderGame
import org.bukkit.Location
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.math.V3
import ru.cristalix.core.util.UtilV3

abstract class BlockInteract(
    private val trigger: V3, override val gold: Int, override val title: String
) : Interactive<PlayerInteractEvent>(gold, title) {

    var triggerBlock: Location? = null

    lateinit var game: MurderGame // todo будет это работать?

    override fun init(game: MurderGame) {
        this.game = game

        if (triggerBlock == null) triggerBlock = UtilV3.toLocation(trigger, game.map.world)

        createInteractiveTitle(triggerBlock!!, title)
        createInteractiveTitle(triggerBlock!!.clone().subtract(0.0, 0.3, 0.0), "§e§l$gold золота")
        createInteractiveTitle(triggerBlock!!.clone().subtract(0.0, 0.7, 0.0), "§bКЛИК!")
    }

    override fun trigger(event: PlayerInteractEvent): Boolean {
        return (event.hasBlock() &&
                triggerBlock!!.distanceSquared(event.blockClicked.location) < 15)
                || (!event.hasBlock() && triggerBlock!!.distanceSquared(event.player.location) < 15)
    }
}
