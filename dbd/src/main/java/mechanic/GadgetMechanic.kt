package mechanic

import ENGINE_NEEDED
import clepto.bukkit.Cycle
import dev.implario.bukkit.item.item
import killer
import me.func.commons.light
import me.func.commons.mod.ModHelper
import mechanic.engine.EngineManager
import murder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.potion.PotionEffectType

object GadgetMechanic : Listener {

    private const val REGEN_TIME = 10
    val blindness = org.bukkit.potion.PotionEffect(PotionEffectType.BLINDNESS, Int.MAX_VALUE, 1)
    private val slowness = org.bukkit.potion.PotionEffect(PotionEffectType.SLOW, 20 * REGEN_TIME, 5)
    val bandage = item {
        text("§bБинт")
        type = Material.PAPER
    }.build()

    @EventHandler
    fun PlayerItemHeldEvent.handle() {
        val was = player.inventory.getItem(previousSlot)
        if (was == light)
            player.addPotionEffect(blindness)
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (player == killer?.player || !hasItem())
            return

        val user = murder.getUser(player)

        // Фонарик
        if (item == light && player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            if (user.lightTicks > 0)
                player.removePotionEffect(PotionEffectType.BLINDNESS)
            else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§cЗакончилась зарядка!"))
                return
            }

            Cycle.run(1, user.lightTicks * 3) {
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS) || player.itemInHand != light || user.lightTicks < 0) {
                    player.addPotionEffect(blindness)
                    Cycle.exit()
                    return@run
                } else {
                    user.lightTicks--
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§l${user.lightTicks / 20} §fсек. осталось"))
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
                if (originalLocation.distanceSquared(player.location) > 1.2) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§cВы порвали бинт!"))
                    Cycle.exit()
                } else {
                    player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent("§l${REGEN_TIME - (it / 20)} §fсек. осталось")
                    )
                }
                if (it == 20 * REGEN_TIME - 1) {
                    user.hearts++
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§aРана заделана!"))
                }
            }
        }
        isCancelled = true
    }

}