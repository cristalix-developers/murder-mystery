package me.func.murder.map.interactive

import me.func.murder.MurderGame
import me.func.murder.app
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.math.V3
import ru.cristalix.core.util.UtilV3

abstract class BlockInteract(
    private val trigger: V3, //
    override val gold: Int, //
    override val title: String //
) : Interactive<PlayerInteractEvent>(gold, title) {

    val triggerBlock: MutableMap<MurderGame, Location?> = hashMapOf() // да игры не удаляются я знаю, лень сделать)

    override fun init(game: MurderGame) {
        if (triggerBlock[game] == null) triggerBlock[game] = UtilV3.toLocation(trigger, game.map.world)

        createInteractiveTitle(triggerBlock[game]!!, title)
        createInteractiveTitle(triggerBlock[game]!!.clone().subtract(0.0, 0.3, 0.0), "§e§l$gold золота")
        createInteractiveTitle(triggerBlock[game]!!.clone().subtract(0.0, 0.7, 0.0), "§bКЛИК!")
    }

    fun getGame(player: Player): MurderGame = app.node.linker.getGameByPlayer(player) as MurderGame

    override fun trigger(event: PlayerInteractEvent): Boolean {
        val game = getGame(event.player)

        return (event.hasBlock() && triggerBlock[game]!!.distanceSquared(event.blockClicked.location) < 15)
                    || (!event.hasBlock() && triggerBlock[game]!!.distanceSquared(event.player.location) < 15)
    }
}
