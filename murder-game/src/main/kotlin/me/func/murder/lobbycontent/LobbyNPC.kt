package me.func.murder.lobbycontent

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.murder.app
import me.func.murder.donate.DonateHelper
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.MoneyFormatter
import me.func.murder.donate.impl.Corpse
import me.func.murder.donate.impl.NameTag
import me.func.murder.donate.impl.StepParticle
import me.func.murder.user.User
import me.func.murder.util.GoldRobber
import me.func.murder.worldMeta
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.util.function.Consumer

class LobbyNPC {

    private val backItem = item {
        type = Material.CLAY_BALL
        text("§cНазад")
        nbt("other", "cancel")
    }.build()

    private val accessItem = item {
        text("§aКупить\n\n§7Это кнопка подтверждения\n§7покупки.")
        nbt("other", "access")
        enchant(Enchantment.LUCK, 1)
        type = Material.CLAY_BALL
    }.build()

    private val menu = ControlledInventory.builder()
        .title("MurderMystery")
        .rows(1)
        .columns(9)
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                contents.setLayout("XLXKXPXXS")

                val user = app.getUser(player)
                val stat = user.stat

                contents.add('S', ClickableItem.empty(item {
                    type = Material.PAPER
                    text("§bСтатистика\n\n§7Убийств: §c${stat.kills}\n§7Побед: §b${stat.wins}\n${MoneyFormatter.texted(stat.money)}\n§aСыграно ${stat.games} игр(ы)")
                }.build()))

                contents.add('P', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "g2")
                    text("§bМогилы\n\n§7Выберите могилу, которая\n§7появится на месте\n§7вашей смерти.")
                }.build()) {
                    subInventory(player, 1) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout("XIIIIIXBX")
                        pasteItems(user, currentContent, Corpse.values().filter { it != Corpse.NONE }) {
                            user.stat.activeCorpse = it as Corpse
                        }
                    }
                })
                contents.add('K', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "guild_members_add")
                    text("§bЧастицы ходьбы\n\n§7Выберите тип частиц,\n§7которые будут появлять\n§7следом за вами.")
                }.build()) {
                    subInventory(player, 3) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        pasteItems(user, currentContent, StepParticle.values().asIterable()) {
                            user.stat.activeParticle = it as StepParticle
                        }
                    }
                })
                contents.add('L', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "new_booster_2")
                    text("§bПсевдонимы\n\n§7Выберите псевдоним,\n§7который появится в\n§7табе и над вами.")
                }.build()) {
                    subInventory(player, 3) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        pasteItems(user, currentContent, NameTag.values().asIterable()) {
                            user.stat.activeNameTag = it as NameTag
                        }
                    }
                })
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
                .onClick { it.performCommand("menu") }
                .build()
        )

        // Команда для открытия меню
        B.regCommand({ player, _ ->
            menu.open(player)
            null
        }, "menu", "help")
    }

    fun pasteItems(user: User, content: InventoryContents, item: Iterable<out DonatePosition>, fill: (DonatePosition) -> Unit) {
        item.forEach { currentItem ->
            content.add('I', ClickableItem.of(DonateHelper.modifiedItem(user, currentItem)) {
                if (user.stat.donate.contains(currentItem)) {
                    fill(currentItem)
                    user.player!!.closeInventory()
                } else {
                    donateMenu(user.player!!, currentItem, false)
                }
            })
        }
        content.add('B', ClickableItem.of(backItem) { user.player!!.performCommand("menu") })
    }

    fun subInventory(player: Player, rows: Int, inventory: (Player, InventoryContents) -> Any) {
        ControlledInventory.builder()
            .title("MurderMystery")
            .rows(rows)
            .columns(9)
            .provider(object : InventoryProvider {
                override fun init(player: Player, contents: InventoryContents) {
                    inventory(player, contents)
                    contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
                }
            }).build()
            .open(player)
    }

    private fun donateMenu(player: Player, donatePosition: DonatePosition, realMoney: Boolean) {
        subInventory(player, 1) { _, contents: InventoryContents ->
            contents.setLayout("XOXXXXGBX")
            contents.add('O', ClickableItem.empty(donatePosition.getIcon()))
            contents.add('G', ClickableItem.of(accessItem) {
                val user = app.getUser(player)
                if (realMoney)
                    buy(user, donatePosition.getPrice(), donatePosition.getTitle()) { donatePosition.give(user) }
                else {
                    if (user.stat.donate.contains(donatePosition)) {
                        player.sendMessage(Formatting.error("У вас уже есть этот товар."))
                        player.closeInventory()
                    } else if (donatePosition.getPrice() > user.stat.money) {
                        player.sendMessage(Formatting.error("Не хватает денег :<"))
                        player.closeInventory()
                    } else {
                        user.stat.money -= donatePosition.getPrice()
                        donatePosition.give(user)
                        GoldRobber.forceTake(user, donatePosition.getPrice(), false)
                        player.sendMessage(Formatting.fine("Успешно!"))
                        player.closeInventory()
                    }
                }
            })
            contents.add('B', ClickableItem.of(backItem) { player.performCommand("menu") })
        }
    }

    private fun buy(user: User, money: Int, desc: String, accept: Consumer<User>) {
        val player = user.player!!
        ISocketClient.get().writeAndAwaitResponse<MoneyTransactionResponsePackage>(
            MoneyTransactionRequestPackage(player.uniqueId, money, true, desc)
        ).thenAccept {
            if (it.errorMessage != null) {
                player.sendMessage(Formatting.error(it.errorMessage))
                return@thenAccept
            }
            if (!user.session.isActive) {
                player.sendMessage(Formatting.error("Что-то пошло не так... Попробуйте перезайти"))
                return@thenAccept
            }
            accept.accept(user)
            player.closeInventory()
            player.sendMessage(Formatting.fine("Спасибо за поддержку разработчиков!"))
        }
    }

}