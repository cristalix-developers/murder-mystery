/*
package me.func.murder.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import me.func.commons.arrow
import me.func.commons.donate.impl.Corpse
import me.func.commons.donate.impl.KillMessage
import me.func.commons.mod.ModHelper
import me.func.commons.user.Role
import me.func.commons.user.User
import me.func.commons.util.LocalModHelper
import me.func.commons.util.StandHelper
import me.func.murder.*
import me.func.murder.util.droppedBowManager
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.*
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.EulerAngle


object DamageListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.handle() {

    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        quitMessage = null
        if (activeStatus != Status.GAME)
            return
        // Если важная роль вышла из игры, то важно отметить
        kill(app.getUser(player), null)
    }

    @EventHandler
    fun ProjectileLaunchEvent.handle() {
        if (getEntity() is Arrow && getEntity().shooter is CraftPlayer) {
            (getEntity() as Arrow).pickupStatus = Arrow.PickupStatus.DISALLOWED
            val user = app.getUser(getEntity().shooter as Player)
            user.player!!.inventory.removeItem(arrow)
            if (user.role == Role.DETECTIVE) {
                ModHelper.sendCooldown(user, "Перезарядка лука", 110)
                B.postpone(100) { user.player!!.inventory.setItem(20, arrow) }
            }
        }
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.gameMode != GameMode.SPECTATOR && to.block.isLiquid)
            kill(app.getUser(player), null)
    }


    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus != Status.GAME)
            return
        val user = app.getUser(player)

        // Если маньяк нажал на меч, то запустить его вперед
        if (user.role == Role.MURDER && action == Action.RIGHT_CLICK_AIR && material == Material.IRON_SWORD) {
            user.player!!.playSound(user.player!!.location, Sound.BLOCK_CLOTH_STEP, 1.1f, 1f)
            val sword = StandHelper(player.location.clone().add(0.0, 1.0, 0.0))
                .invisible(true)
                .marker(true)
                .gravity(false)
                .child(true)
                .markTrash()
                .build()
            val nmsSword = (sword as CraftArmorStand).handle
            sword.itemInHand = player.itemInHand
            player.itemInHand = null
            sword.rightArmPose = EulerAngle(
                Math.toRadians(350.0),
                Math.toRadians(player.location.pitch * -1.0),
                Math.toRadians(90.0)
            )
            sword.isCollidable = false
            sword.setSilent(true)

            val vector = player.eyeLocation.direction.normalize()

            val giveBackTime = 5 * 20

            ModHelper.sendCooldown(user, "Возвращение орудия", giveBackTime + 10)
            Cycle.run(1, giveBackTime) { iteration ->
                if (iteration == giveBackTime - 1) {
                    player.inventory.setItem(1, sword.itemInHand)
                    sword.remove()
                }

                val origin = sword.location.clone().add(0.0, 1.0, -0.4)

                sword.world.spawnParticle(Particle.DRIP_LAVA, origin, 1)

                nmsSword.move(EnumMoveType.SELF, 0.0, 0.0, 0.0)
                if (sword.location.clone().add(
                        vector.x / 100 - vector.x / 10000,
                        vector.y / 100 - vector.y / 10000 + 1.4,
                        vector.z / 100 - vector.z / 10000
                    ).block.type != Material.AIR
                )
                    return@run
                nmsSword.move(EnumMoveType.SELF, vector.x / 1.6, vector.y / 1.6, vector.z / 1.6)
                Bukkit.getOnlinePlayers().minus(player)
                    .filter { it.gameMode != GameMode.SPECTATOR && it.location.distanceSquared(sword.location) < 1.5 }
                    .forEach {
                        it.damage(10.0, player)
                    }
            }
        }
    }

    @EventHandler
    fun EntityDamageEvent.handle() {
        isCancelled = true

        if (activeStatus == Status.GAME && damage < 0.01) {
            if (entity is CraftPlayer) {
                kill(app.getUser(entity as CraftPlayer), null)
            }
        }
    }
}*/
