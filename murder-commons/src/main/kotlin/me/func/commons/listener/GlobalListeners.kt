package me.func.commons.listener

import clepto.bukkit.B
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import io.netty.buffer.Unpooled
import me.func.commons.app
import me.func.commons.content.DailyRewardManager
import me.func.commons.content.WeekRewards
import me.func.commons.getByPlayer
import me.func.commons.mod.ModHelper
import me.func.commons.worldMeta
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Location
import org.bukkit.entity.Arrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkLoadEvent
import ru.cristalix.core.display.DisplayChannels
import ru.cristalix.core.display.messages.Mod
import ru.cristalix.core.formatting.Formatting
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

object GlobalListeners : Listener {

    // Получении точки спавна
    private var spawn: Location? = null

    // Прогрузка файлов модов
    private var modList = try {
        File("./mods/").listFiles()!!
            .map {
                val buffer = Unpooled.buffer()
                buffer.writeBytes(Mod.serialize(Mod(Files.readAllBytes(it.toPath()))))
                buffer
            }.toList()
    } catch (exception: Exception) {
        Collections.emptyList()
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        if (spawn == null) {
            val dot = worldMeta.getLabel("spawn")
            val args = dot.tag.split(" ")
            if (args.size > 1) {
                dot.setYaw(args[0].toFloat())
                dot.setPitch(args[1].toFloat())
            }
            spawn = dot.add(0.5, 0.0, 0.5)
        }

        val user = getByPlayer(player)

        // Отправка модов
        B.postpone(1) {
            player.teleport(spawn)
            modList.forEach {
                user.sendPacket(
                    PacketPlayOutCustomPayload(
                        DisplayChannels.MOD_CHANNEL,
                        PacketDataSerializer(it.retainedSlice())
                    )
                )
            }
            ModHelper.updateBalance(user)
        }
        B.postpone(10) {
            val now = System.currentTimeMillis()
            // Обнулить комбо сбора наград если прошло больше суток или комбо >7
            if ((user.stat.rewardStreak > 0 && now - user.stat.lastEnter > 24 * 60 * 60 * 1000) || user.stat.rewardStreak > 6) {
                user.stat.rewardStreak = 0
            }
            if (now - user.stat.dailyClaimTimestamp > 14 * 60 * 60 * 1000) {
                user.stat.dailyClaimTimestamp = now
                DailyRewardManager.open(user)

                val dailyReward = WeekRewards.values()[user.stat.rewardStreak]
                player.sendMessage(Formatting.fine("Ваша ежедневная награда: " + dailyReward.title))
                dailyReward.give(user)
                user.stat.rewardStreak++
            }
        }
    }

    @EventHandler
    fun BlockPlaceEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockRedstoneEvent.handle() {
        newCurrent = oldCurrent
    }

    @EventHandler
    fun CraftItemEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEntityEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFadeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockSpreadEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockGrowEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFromToEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun HangingBreakByEntityEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockBurnEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun EntityExplodeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerAdvancementCriterionGrantEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun InventoryClickEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun FoodLevelChangeEvent.handle() {
        foodLevel = 20
    }

    @EventHandler
    fun ProjectileHitEvent.handle() {
        if (entity is Arrow)
            entity.remove()
    }
}