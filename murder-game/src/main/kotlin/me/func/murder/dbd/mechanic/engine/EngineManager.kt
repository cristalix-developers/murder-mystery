package me.func.murder.dbd.mechanic.engine

import dev.implario.bukkit.event.on
import me.func.murder.MurderGame
import me.func.murder.Status
import me.func.murder.dbd.mechanic.drop.ChestManager
import me.func.murder.getUser
import me.func.murder.mod.ModHelper
import me.func.murder.util.Music
import me.func.murder.util.StandHelper
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class EngineManager(private val game: MurderGame) {

    private val light: List<Location> = game.map.getLabels("light")
    var engines: Map<Engine, Location> = game.map.getLabels("engine").associate {
        Engine(
            it, 0, StandHelper(it.toCenterLocation().subtract(0.0, 1.2, 0.0))
                .gravity(false)
                .invisible(true)
                .name("§lДвигатель §e0%")
                .marker(true)
                .build()
        ) to light.minByOrNull { light -> light.distanceSquared(it) }!!.clone().subtract(0.0, 3.0, 0.0)
    }

    fun clearAll() {
        engines.forEach { (key, value) ->
            key.percent = 0
            key.stand?.customName = "§lДвигатель §e0%"
            value.block.setTypeAndDataFast(Material.IRON_BLOCK.id, 0.toByte())
            value.clone().add(0.0, 2.0, 0.0).block.setTypeAndDataFast(95, 14)
        }
    }

    fun enginesDone(): Int {
        return engines.keys.filter { engine -> engine.percent >= 100 }.size
    }

    init {
        game.context.on<PlayerInteractEvent> { handle() }
    }

    fun PlayerInteractEvent.handle() {
        if (hand != EquipmentSlot.OFF_HAND)
            return
        if (game.activeStatus == Status.GAME && hasBlock() && player.gameMode != GameMode.SPECTATOR && player !=
            game.killer?.player
        ) {
            engines.filter { entry -> entry.key.percent < 100 && entry.key.location.distanceSquared(blockClicked.location) < 14 }
                .forEach { entry ->
                    if (player.inventory.getItem(2) != ChestManager.fuel) {
                        player.spigot()
                            .sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§cНайдите топливо в сундуке!"))
                        return
                    }

                    player.inventory.setItem(2, null)
                    entry.key.percent += minOf(25, 100 - entry.key.percent)

                    val percent = entry.key.percent

                    entry.key.stand?.customName = "§lДвигатель §e$percent%"
                    val glass = entry.value.clone().add(0.0, 2.0, 0.0).block

                    val color = when (percent) {
                        in 1..25 -> {
                            glass.setTypeAndDataFast(95, 14)
                            "§4"
                        }
                        in 25..50 -> {
                            glass.setTypeAndDataFast(95, 1)
                            "§6"
                        }
                        in 50..75 -> {
                            glass.setTypeAndDataFast(95, 4)
                            "§e"
                        }
                        else -> {
                            entry.value.block.setTypeAndDataFast(0, 0)
                            "§2"
                        }
                    }
                    var percentLine = color
                    repeat(percent * 8 / 100) { percentLine += "■" }
                    percentLine += "§0"
                    repeat(8 - percent * 8 / 100) { percentLine += "■" }

                    val user = game.userManager.getUser(player)
                    user.fuel++
                    ModHelper.sendTitle(user, "${color + percent}%\n\n\n$percentLine §4⛽")

                    val haveEngines = enginesDone()

                    if (percent == 100) {
                        if (haveEngines < MurderGame.ENGINE_NEEDED) {
                            game.broadcast(
                                "  §l> §bИгрок §e${player.name} §bисправил движок! Осталось еще §e${
                                    MurderGame
                                        .ENGINE_NEEDED - haveEngines
                                }"
                            )

                            player.world.strikeLightning(entry.key.location)
                            Music.LIGHT_OFF.playAll(game)
                            game.context.after(20 * 10) { Music.DBD_GAME.playAll(game) }
                        } else if (haveEngines == MurderGame.ENGINE_NEEDED) {
                            game.modHelper.sendGlobalTitle("§aДвигатели активированы\n§d✨✨✨")
                            game.broadcast("")
                            game.broadcast("  §l> §aИгрок §e${player.name} §aисправил последний движок! §b§lОткрывайте врата")
                            game.broadcast("")
                            game.gateManager!!.gates.forEach {
                                it.stand.customName =
                                    "§eПодойдите, чтобы открыть врата"
                            }
                        }
                    }
                }
        }
    }
}
