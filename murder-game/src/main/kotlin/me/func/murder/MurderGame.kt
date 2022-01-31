package me.func.murder

import dev.implario.bukkit.item.item
import dev.implario.games5e.node.Game
import dev.implario.games5e.sdk.cristalix.Cristalix
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

val gold: ItemStack = item {
    type = Material.GOLD_INGOT
    text("§eЗолото\n\n§7Соберите §e10 штук§7,\n§7и получите §bлук§7!\n§7Или покупайте действия\n§7на карте.")
}

val arrow: ItemStack = item {
    type = Material.ARROW
    text("§bСтрела")
}

val light: ItemStack = item {
    type = Material.CLAY_BALL
    nbt("thief", "4")
    text("§6Фонарик §l§eПКМ")
}

class MurderGame(gameId: UUID) : Game(gameId) {

    lateinit var murderName: String
    lateinit var detectiveName: String
    lateinit var winMessage: String
    var heroName = ""

    private val cristalix: Cristalix = Cristalix.connectToCristalix(this, "MRD", "MurderMystey")
    private val map: WorldMeta = MapLoader.load(this, "MurderMystey", "prod") // TODO: setup?

    init {
        GameListeners( this)
    }

    override fun acceptPlayer(e: AsyncPlayerPreLoginEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSpawnLocation(uuid: UUID): Location {
        TODO("Not yet implemented")
    }
}
