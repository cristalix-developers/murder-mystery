package me.func.murder.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import me.func.murder.Status
import me.func.murder.activeStatus
import me.func.murder.app
import me.func.murder.user.Role
import me.func.murder.user.User
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
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
                if (userVictim.role == Role.DETECTIVE) {
                    killDetective(userVictim)
                } else if (userVictim.role == Role.VILLAGER) {
                    B.bc(Formatting.error("§e${victim.name} §fбыл убит маньяком."))
                }
            } else if (byArrow) {
                when (userVictim.role) {
                    Role.MURDER -> killMurder(userVictim)
                    Role.DETECTIVE -> killDetective(userVictim)
                    else -> B.bc(Formatting.error("§e${victim.name} §fбыл убит."))
                }
            } else
                return
            victim.sendTitle("§cПоражение", "§cвы были убиты")
            victim.gameMode = GameMode.SPECTATOR
            userVictim.role = Role.NONE
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        quitMessage = null
        if (activeStatus != Status.GAME)
            return
        val user = app.getUser(player)
        // Если важная роль вышла из игры, то важно отметить
        when (user.role) {
            Role.DETECTIVE -> killDetective(user)
            Role.MURDER -> killMurder(user)
            Role.VILLAGER -> B.bc(Formatting.error("§e${player.name} §fубежал от игры."))
            else -> {
            }
        }
    }

    @EventHandler
    fun ProjectileLaunchEvent.handle() {
        if (getEntity() is Arrow)
            B.postpone(60) { (getEntity().shooter as Player).inventory.setItem(20, ItemStack(Material.ARROW)) }
    }

    private fun killDetective(user: User) {
        // Сообщение о выпадении лука
        Bukkit.getOnlinePlayers().forEach { it.sendTitle("§eЛук выпал", "§cДетектив убит") }
        // Выпадение лука
        val bow = user.player!!.world.spawnEntity(
            user.player!!.location.clone().subtract(0.0, 1.0, 0.0),
            EntityType.ARMOR_STAND
        )
        bow.setMetadata("detective", FixedMetadataValue(app, true))
        bow.isInvulnerable = true
        val nmsBow = (bow as CraftArmorStand).handle
        nmsBow.isMarker = true
        nmsBow.isInvisible = true
        nmsBow.isNoGravity = true
        nmsBow.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(ItemStack(Material.BOW)))
    }

    private fun killMurder(user: User) {
        // Детектив/Мирный житель убивает с лука убийцу
        Bukkit.getOnlinePlayers().minus(user.player!!).forEach {
            it.sendTitle("§aПобеда мирных", "§aМаньяк уничтожен")
            it.gameMode = GameMode.SPECTATOR
        }
        B.bc(Formatting.fine("§bМаньяк был убит!"))
        activeStatus = Status.END
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus != Status.GAME)
            return
        val user = app.getUser(player)

        // Если маньяк нажал на меч, то запустить его вперед
        if (user.role == Role.MURDER && action == Action.RIGHT_CLICK_AIR && material == Material.IRON_SWORD) {
            val sword =
                player.world.spawnEntity(player.location.clone().add(0.0, 1.0, 0.0), EntityType.ARMOR_STAND)
            sword.isInvulnerable = true
            val nmsSword = (sword as CraftArmorStand).handle
            nmsSword.isMarker = true
            nmsSword.isInvisible = true
            nmsSword.isNoGravity = true
            nmsSword.isSmall = true
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

            Cycle.run(1, 20 * 3) { iteration ->
                if (iteration == 20 * 3 - 1) {
                    player.inventory.setItem(2, sword.itemInHand)
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
                    .filter { it.gameMode != GameMode.SPECTATOR && it.location.distanceSquared(sword.location) < 3.4 }
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