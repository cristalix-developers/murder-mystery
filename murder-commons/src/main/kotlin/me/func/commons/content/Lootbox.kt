package me.func.commons.content

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.impl.Corpse
import me.func.commons.donate.impl.NameTag
import me.func.commons.donate.impl.StepParticle
import me.func.commons.getByPlayer
import me.func.commons.gold
import me.func.commons.mod.ModTransfer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider

class Lootbox : Listener {

    private val dropList = Corpse.values().map { it }
        .plus(NameTag.values())
        .plus(StepParticle.values())
        .filter { it != Corpse.NONE && it != NameTag.NONE && it != StepParticle.NONE }

    private val lootboxPrice = 196

    private val lootboxItem = item {
        type = Material.CLAY_BALL
        nbt("other", "enderchest1")
        text("§bЛутбокс\n\n§7Откройте и получите\n§7псевдоним, частицы ходьбы\n§7или скин могилы!\n\n§e > §f㜰 §aОткрыть сейчас за §e$lootboxPrice золота")
    }.build()

    private val lootbox = ControlledInventory.builder()
        .title("Ваши лутбоксы")
        .rows(5)
        .columns(9)
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                val user = getByPlayer(player)

                contents.setLayout(
                    "XOOOOOOOX",
                    "XOOOOOOOX",
                    "XOOOPOOOX",
                    "XOOOOOOOX",
                    "XOOOOOOOX",
                )

                repeat(minOf(user.stat.lootbox, contents.size('O'))) {
                    contents.add('O', ClickableItem.of(lootboxItem) {
                        player.closeInventory()
                        if (user.stat.money < lootboxPrice) {
                            player.sendMessage(Formatting.error("Не хватает монет :("))
                            return@of
                        }
                        user.minusMoney(lootboxPrice)
                        user.stat.lootbox--

                        val drop = dropList.random() as DonatePosition
                        val moneyDrop = (Math.random() * 20 + 10).toInt()

                        ModTransfer()
                            .integer(2)
                            .item(CraftItemStack.asNMSCopy(drop.getIcon()))
                            .string(drop.getTitle())
                            .string(drop.getRare().name)
                            .item(CraftItemStack.asNMSCopy(gold))
                            .string("§e$moneyDrop монет")
                            .string("")
                            .send("lootbox", user)

                        if (user.stat.donate.contains(drop)) {
                            player.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §e32 золота§f."))
                            user.giveMoney(32)
                        } else {
                            user.stat.donate.add(drop)
                        }
                        user.giveMoney(moneyDrop)

                        B.bc(Formatting.fine("§e${player.name} §fполучил §b${drop.getRare().with(drop.getTitle())}."))
                    })
                }
                contents.add('P', ClickableItem.empty(item {
                    type = Material.CLAY_BALL
                    nbt("other", "anvil")
                    text("§bКак их получить?\n\n§7Побеждайте в игре,\n§7и с шансом §a10%\n§7вы получите §bлутбокс§7.")
                }.build()))
                contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
            }
        }).build()

    init {
        B.regCommand({ player, _ ->
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 2f)
            null
        }, "lootboxsound")
    }

    @EventHandler
    fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.ENDER_CHEST) {
            B.postpone(1) { lootbox.open(player as Player) }
        }
    }

}