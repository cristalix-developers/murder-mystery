package me.func.murder.content

import clepto.bukkit.B
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import me.func.murder.MurderGame
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.impl.ArrowParticle
import me.func.murder.donate.impl.Corpse
import me.func.murder.donate.impl.KillMessage
import me.func.murder.donate.impl.Mask
import me.func.murder.donate.impl.NameTag
import me.func.murder.donate.impl.StepParticle
import me.func.murder.getUser
import me.func.murder.mod.ModTransfer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider

class Lootbox(private val game: MurderGame) {

    companion object {
        private const val lootboxPrice = 192
    }

    private val dropList =
        Corpse.values()
            .toList()
            .plus(NameTag.values())
            .plus(StepParticle.values())
            .plus(KillMessage.values())
            .plus(ArrowParticle.values())
            .plus(Mask.values())
            .filter {
                // @formatter:off
                   it != KillMessage.NONE
                && it != Corpse.NONE
                && it != NameTag.NONE
                && it != StepParticle.NONE
                && it != ArrowParticle.NONE
                && it != Mask.NONE
                // @formatter:on
            }

    private val lootboxItem = item {
        type = Material.CLAY_BALL
        nbt("other", "enderchest1")
        text(
            "§bЛутбокс\n\n§7Откройте и получите\n§7псевдоним, частицы ходьбы\n§7следы от стрелы, маски\n§7или скин могилы!\n\n§e > §f㜰 §aОткрыть сейчас за\n${
                MoneyFormatter.texted(lootboxPrice)
            }"
        )
    }

    private val lootbox =
        ControlledInventory.builder().title("Ваши лутбоксы").rows(5).columns(9).provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                val user = game.userManager.getUser(player)

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
                        user.stat.lootboxOpenned++ // todo

                        val drop = dropList.random() as DonatePosition
                        val moneyDrop = (Math.random() * 20 + 10).toInt()

                        ModTransfer().integer(2)
                            .item(CraftItemStack.asNMSCopy(drop.icon))
                            .string(drop.title)
                            .string(drop.rare.name)
                            .item(CraftItemStack.asNMSCopy(MurderGame.gold))
                            .string("§e$moneyDrop монет")
                            .string("")
                            .send("lootbox", user)

                        if (user.stat.donate.contains(drop)) { // todo
                            val giveBack = (drop.rare.ordinal + 1) * 48
                            player.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §e$giveBack монет§f."))
                            user.giveMoney(giveBack)
                        } else {
                            user.stat.donate.add(drop)
                        }
                        user.giveMoney(moneyDrop)

                        game.broadcast(
                            Formatting.fine(
                                "§e${player.name} §fполучил §b${
                                    drop.rare.with(drop.title)
                                }."
                            )
                        )
                    })
                }
                contents.add('P', ClickableItem.empty(item {
                    type = Material.CLAY_BALL
                    nbt("other", "anvil")
                    text("§bКак их получить?\n\n§7Побеждайте в игре,\n§7и с шансом §a10%\n§7вы получите §bлутбокс§7.")
                }))
                contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
            }
        }).build()

    init {
        game.context.on<InventoryOpenEvent> {
            if (inventory.type == InventoryType.ENDER_CHEST) {
                isCancelled = true
                lootbox.open(player as Player)
            }
        }

        B.regCommand({ player, _ ->
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 2f)
            null
        }, "lootboxsound")
        B.regCommand({ player, _ ->
            DailyRewardManager.open(game.userManager.getUser(player))
            null
        }, "secrethook")
    }
}
