package me.func.murder.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import dev.implario.bukkit.item.item
import me.func.murder.*
import me.func.murder.mod.ModHelper
import me.func.murder.user.Role
import me.func.murder.user.User
import me.func.murder.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.*
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.EulerAngle
import ru.cristalix.core.formatting.Formatting


class DamageListener : Listener {

    @EventHandler
    fun EntityDamageByEntityEvent.handle() {
        cancelled = true

        if (activeStatus != Status.GAME)
            return

        val victim = entity
        if (victim is Player) {
            var byArrow = false
            // Получение убийцы
            val killer = when (damager) {
                is Player -> damager as Player
                is Projectile -> {
                    byArrow = true
                    (damager as Projectile).shooter as Player
                }
                else -> return
            }
            // Проверки на роли
            val userVictim = app.getUser(victim)
            val userKiller = app.getUser(killer)
            if (userKiller == userVictim)
                return
            if (userKiller.role == Role.MURDER || (userKiller.role == Role.MURDER && byArrow)) {
                if (killer.itemInHand.getType() != Material.IRON_SWORD && damage != 10.0)
                    return
                // Убийца убивает с меча или с лука
                userKiller.giveMoney(2)
                userKiller.stat.kills++
                kill(userVictim)
                val sword = killer.inventory.getItem(1)
                killer.inventory.setItem(1, null)
                ModHelper.sendCooldown(userKiller, "Возвращение меча", 50)
                B.postpone(50) { killer.inventory.setItem(1, sword) }
                return
            } else if (byArrow) {
                if (userVictim.role == Role.MURDER) {
                    heroName = userKiller.name
                    userKiller.giveMoney(5)
                } else
                    kill(userKiller)
                kill(userVictim)
            } else
                return
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        quitMessage = null
        if (activeStatus != Status.GAME)
            return
        // Если важная роль вышла из игры, то важно отметить
        kill(app.getUser(player))
    }

    @EventHandler
    fun ProjectileLaunchEvent.handle() {
        if (getEntity() is Arrow && getEntity().shooter is CraftPlayer) {
            val user = app.getUser(getEntity().shooter as Player)
            user.player!!.inventory.remove(Material.ARROW)
            if (user.role == Role.DETECTIVE) {
                ModHelper.sendCooldown(user, "Перезарядка лука", 120)
                B.postpone(120) { user.player!!.inventory.setItem(20, ItemStack(Material.ARROW)) }
            }
        }
    }

    private fun kill(victim: User) {
        val player = victim.player!!
        if (player.gameMode == GameMode.SPECTATOR)
            return
        if (victim.role == Role.DETECTIVE)
            killDetective(victim)
        if (victim.role == Role.MURDER)
            killMurder(victim)
        B.bc(Formatting.error("§c${victim.name} §fбыл убит."))
        ModHelper.sendTitle(victim, "Вы проиграли")
        player.gameMode = GameMode.SPECTATOR
        player.inventory.clear()
        victim.role = Role.NONE
        ModHelper.makeCorpse(victim)
        ModHelper.update()

        var location = player.location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 30)
        StandHelper(location.clone().subtract(0.0, 1.5, 0.0))
            .marker(true)
            .invisible(true)
            .gravity(false)
            .slot(EnumItemSlot.HEAD, item {
                type = Material.CLAY_BALL
                nbt("other", "g4")
            }.build())
            .markTrash()

        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, 1f, 1f)
        }
    }

    private fun killDetective(user: User) {
        // Сообщение о выпадении лука
        ModHelper.sendGlobalTitle("Лук выпал")
        // Выпадение лука
        StandHelper(user.player!!.location.clone().subtract(0.0, 1.0, 0.0))
            .gravity(false)
            .marker(true)
            .invisible(true)
            .slot(EnumItemSlot.HEAD, ItemStack(Material.BOW))
            .markTrash()
            .fixedData("detective", true)
            .build()
            .isGlowing = true
    }

    private fun killMurder(user: User) {
        // Детектив/Мирный житель убивает с лука убийцу
        Bukkit.getOnlinePlayers().minus(user.player!!).forEach {
            winMessage = "§aМирные жители победили!"
            ModHelper.sendTitle(app.getUser(it), "Победа мирных")
            it.gameMode = GameMode.SPECTATOR
        }
        activeStatus = Status.END
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus != Status.GAME)
            return
        val user = app.getUser(player)

        // Если маньяк нажал на меч, то запустить его вперед
        if (user.role == Role.MURDER && action == Action.RIGHT_CLICK_AIR && material == Material.IRON_SWORD) {
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

            ModHelper.sendCooldown(user, "Возвращение меча", 6 * 20)
            Cycle.run(1, 20 * 6) { iteration ->
                if (iteration == 20 * 6 - 1) {
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
        cancelled = true

        if (activeStatus == Status.GAME && cause == EntityDamageEvent.DamageCause.FIRE_TICK)
            isCancelled = false
    }
}