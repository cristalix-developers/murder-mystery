package mechanic

import ENGINE_NEEDED
import clepto.bukkit.Cycle
import dev.implario.bukkit.item.item
import killer
import me.func.commons.light
import me.func.commons.mod.ModHelper
import me.func.commons.util.StandHelper
import mechanic.engine.EngineManager
import murder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

object GadgetMechanic : Listener {

    private const val REGEN_TIME = 7
    val slowness = org.bukkit.potion.PotionEffect(PotionEffectType.SLOW, 20 * REGEN_TIME, 10)
    val traps = mutableListOf<ArmorStand>()
    val blindness = org.bukkit.potion.PotionEffect(PotionEffectType.BLINDNESS, Int.MAX_VALUE, 1)
    val bandage: ItemStack = item {
        text("§bБинт")
        type = Material.PAPER
    }.build()
    val openTrap: ItemStack = item {
        text("§eКапкан")
        nbt("helloween", "kapkan_1")
        type = Material.CLAY_BALL
    }.build()
    val closeTrap: ItemStack = item {
        nbt("helloween", "kapkan2")
        type = Material.CLAY_BALL
    }.build()

    @EventHandler
    fun PlayerItemHeldEvent.handle() {
        val was = player.inventory.getItem(previousSlot)
        if (was == light)
            player.addPotionEffect(blindness)
    }

    @EventHandler
    fun PlayerFishEvent.handle() {
        if (state == PlayerFishEvent.State.FISHING) {
            val user = murder.getUser(player)

            if (player == killer?.player) {
                if (user.countdown == 0) {
                    user.countdown = 20 * 20
                    Cycle.run(1, 20 * 20) {
                        user.countdown = maxOf(0, user.countdown - 1)
                    }
                } else {
                    player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent("§cХук будет доступен через §l§f${user.countdown / 20} сек.")
                    )
                    isCancelled = true
                }
            }
        } else if (state == PlayerFishEvent.State.CAUGHT_ENTITY && entity is CraftPlayer) {
            entity.velocity = player.location.toVector().subtract(entity.location.toVector()).multiply(0.3)
            entity.velocity.y = 0.4
        } else
            cancel = true
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (!hasItem())
            return

        if (player == killer?.player && action == Action.RIGHT_CLICK_BLOCK && player.inventory.getItem(3) != null) {
            player.inventory.setItem(3, null)

            traps.add(
                StandHelper(blockClicked.location.toCenterLocation().subtract(0.0, 1.2, 0.0))
                    .markTrash()
                    .invisible(true)
                    .marker(true)
                    .slot(EnumItemSlot.HEAD, openTrap)
                    .gravity(false)
                    .build()
            )
            killer!!.giveMoney(1)
            return
        }

        if (player == killer?.player)
            return

        val user = murder.getUser(player)

        // Фонарик
        if (item == light && player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            if (user.lightTicks > 0) {
                player.removePotionEffect(PotionEffectType.BLINDNESS)
                player.isSprinting = false
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§cЗакончилась зарядка!"))
                return
            }

            Cycle.run(1, user.lightTicks * 3) {
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS) || player.itemInHand != light || user.lightTicks < 0) {
                    player.addPotionEffect(blindness)
                    player.isSprinting = false
                    Cycle.exit()
                    return@run
                } else {
                    user.lightTicks--
                    player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent("§l${user.lightTicks / 20} §fсек. осталось")
                    )
                }
            }
        } else if (ENGINE_NEEDED > EngineManager.enginesDone()) {
            player.addPotionEffect(blindness)
        }
        // Бинты
        if (item == bandage && user.hearts < 2 && player.gameMode != GameMode.SPECTATOR) {
            player.itemInHand = null
            player.addPotionEffect(slowness)

            val originalLocation = player.location

            ModHelper.sendTitle(user, "Заживление раны...\nНе двигайтесь!")

            Cycle.run(1, 20 * REGEN_TIME) {
                if (it == 20 * REGEN_TIME - 1) {
                    user.hearts++
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§aРана заделана!"))
                    return@run
                }
                if (originalLocation.distanceSquared(player.location) > 1.2 || user.hearts > 1) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§cВы порвали бинт!"))
                    Cycle.exit()
                } else {
                    player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent("§l${REGEN_TIME - (it / 20)} §fсек. осталось")
                    )
                }
            }
        }
        isCancelled = true
    }

    fun clearTraps() {
        traps.clear()
    }

}