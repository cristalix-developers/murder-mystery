package me.func.murder.lobbycontent

import dev.implario.bukkit.item.item
import me.func.murder.app
import me.func.murder.worldMeta
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs

class LobbyNPC {

    private val menu = ControlledInventory.builder()
        .title("MurderMystery")
        .rows(1)
        .columns(9)
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                contents.setLayout(
                    "XXXXSXXXX",
                )

                val user = app.getUser(player)
                val stat = user.stat

                contents.add('S', ClickableItem.empty(item {
                    type = Material.PAPER
                    text("§bСтатистика\n\n§fУбийств: §c${stat.kills}\n§fПобед: §b${stat.wins}\n§fМонет: §e${stat.money}\n§aСыграно ${stat.games} игр(ы)")
                }.build()))
                contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
            }
        }).build()

    init {
        // Создание NPC в лобби
        Npcs.init(app)
        val npcLabel = worldMeta.getLabel("guide")
        val npcArgs = npcLabel.tag.split(" ")
        npcLabel.setYaw(npcArgs[0].toFloat())
        npcLabel.setPitch(npcArgs[1].toFloat())
        Npcs.spawn(
            Npc.builder()
                .location(npcLabel)
                .name("§d§lMurder§f§lMystery")
                .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                .skinUrl("https://webdata.c7x.dev/textures/skin/307264a1-2c69-11e8-b5ea-1cb72caa35fd")
                .skinDigest("307264a1-2c69-11e8-b5ea1cb72caa35fd")
                .type(EntityType.PLAYER)
                .onClick { player -> menu.open(player) }
                .build()
        )
    }

}