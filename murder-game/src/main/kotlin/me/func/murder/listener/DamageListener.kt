package me.func.murder.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import me.func.murder.Status
import me.func.murder.activeStatus
import me.func.murder.app
import me.func.murder.user.Role
import me.func.murder.user.User
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
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
            if (userKiller.role == Role.MURDER || byArrow) {
                if (killer.itemInHand.getType() != Material.IRON_SWORD)
                    return
                // Убийца убивает с меча или с лука
                if (userVictim.role == Role.DETECTIVE) {
                    killDetective(userVictim)
                } else if (userVictim.role == Role.VILLAGER) {
                    B.bc(Formatting.error("§e${victim.name} §fбыл убит маньяком."))
                }
            } else if (userVictim.role == Role.MURDER && byArrow) {
                killMurder(userVictim)
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
            else -> {}
        }
    }

    private fun killDetective(user: User) {
        // Сообщение о выпадении лука
        Bukkit.getOnlinePlayers().forEach { it.sendTitle("§eЛук выпал", "§cДетектив убит") }
        // Выпадение лука
        val bow =
            user.player!!.world.spawnEntity(user.player!!.location.clone().add(0.0, 1.0, 0.0), EntityType.ARMOR_STAND)
        bow.setMetadata("detective", FixedMetadataValue(app, true))
        bow.isInvulnerable = true
        val nmsBow = (bow as CraftArmorStand).handle
        nmsBow.isMarker = true
        nmsBow.isInvisible = true
        nmsBow.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(ItemStack(Material.BOW)))
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
            player.itemInHand = null

            val sword =
                player.world.spawnEntity(player.location, EntityType.ARMOR_STAND)
            sword.isInvulnerable = true
            val nmsSword = (sword as CraftArmorStand).handle
            nmsSword.isMarker = true
            nmsSword.isInvisible = true
            nmsSword.isNoGravity = true
            nmsSword.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(ItemStack(Material.IRON_SWORD)))
            sword.teleport(player)

            val vector = player.eyeLocation.toVector().normalize()

            Cycle.run(1, 20 * 7) { iteration ->
                if (iteration == 20 * 7 - 1) {
                    sword.remove()
                    user.role.start(user)
                }

                val origin = sword.location.clone().add(0.0, 1.4, 0.0)

                sword.world.spawnParticle(Particle.CLOUD, origin, 2)

                if (sword.location.clone().add(vector.x, vector.y, vector.z).block.type != Material.AIR) {
                    sword.velocity = Vector(0.0, 0.0, 0.0)
                    return@run
                }
                sword.velocity = vector

                Bukkit.getOnlinePlayers().minus(player)
                    .filter { it.location.distanceSquared(origin) < 4 }
                    .forEach { _ -> player.damage(1.0, player) }
            }
        }
    }

    @EventHandler
    fun EntityDamageEvent.handle() {
        cancelled = true

        if (activeStatus == Status.GAME && cause == EntityDamageEvent.DamageCause.FIRE_TICK)
            cancelled = false
    }
}