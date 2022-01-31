@file:Suppress("DEPRECATION")

package me.func.murder

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import me.func.commons.donate.impl.Corpse
import me.func.commons.donate.impl.KillMessage
import me.func.commons.map.interactive.BlockInteract
import me.func.commons.mod.ModHelper
import me.func.commons.mod.ModTransfer
import me.func.commons.user.Role
import me.func.commons.user.User
import me.func.commons.util.LocalModHelper
import me.func.commons.util.Music
import me.func.commons.util.MusicHelper
import me.func.commons.util.StandHelper
import me.func.murder.util.droppedBowManager
import me.func.murder.util.goldManager
import net.minecraft.server.v1_12_R1.DamageSource.arrow
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.cristalix.core.account.IAccountService
import ru.cristalix.core.formatting.Formatting
import java.util.UUID
import java.util.concurrent.TimeUnit

class GameListeners(private val game: MurderGame) {
    private val context = game.context

    init {
        setupChatListeners()
        setupDamageListeners()
        setupGoldListeners()
        setupInteractListeners()
        setupConntecionListeners()
        setupInventoryListeners()
    }

    private fun setupChatListeners() {
        context.on<AsyncPlayerChatEvent> {
            if (player.gameMode == GameMode.SPECTATOR) {
                game.players.forEach {
                    if (it.gameMode == GameMode.SPECTATOR) it.sendMessage(
                        player.name + " >§7 " + ChatColor.stripColor(
                            message
                        )
                    )
                }
                isCancelled = true
                return@on
            }
        }
    }

    private fun setupDamageListeners() {
        context.on<EntityDamageByEntityEvent> {
            isCancelled = true

            if (activeStatus != Status.GAME) return@on

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
                    else -> return@on
                }
                // Проверки на роли
                val userVictim = app.getUser(victim)
                val userKiller = app.getUser(killer)
                if (userKiller == userVictim) {
                    if (byArrow) damager.remove()
                    return@on
                }
                if (userKiller.role == Role.MURDER) {
                    if (byArrow || killer.inventory.itemInMainHand.getType() == Material.IRON_SWORD || damage == 10.0) {
                        // Убийца убивает с меча или с лука
                        userKiller.giveMoney(2)
                        userKiller.stat.kills++
                        kill(userVictim, userKiller)
                        val sword = killer.inventory.getItem(1)
                        killer.inventory.setItem(1, null)
                        ModHelper.sendCooldown(userKiller, "Возвращение орудия", 60)
                        context.after(50) { killer.inventory.setItem(1, sword) }
                        return@on
                    }
                } else if (byArrow) {
                    if (userVictim.role == Role.MURDER) {
                        heroName = userKiller.name
                        userKiller.giveMoney(5)
                    } else kill(userKiller, userKiller)
                    kill(userVictim, userKiller)
                } else return@on
            }
        }
    }

    private fun setupGoldListeners() {
        context.on<PlayerPickupArrowEvent> {
            arrow.remove()
            isCancelled = true
        }

        val bow = item {
            type = Material.BOW
            text("§eЛук")
            nbt("Unbreakable", 1)
        }

        context.on<PlayerAttemptPickupItemEvent> {
            if (item.itemStack.getType() != Material.GOLD_INGOT) {
                item.remove()
                isCancelled = true
                return@on
            }
            val itemStack = player.inventory.getItem(8)
            val user = app.getUser(player)
            user.giveMoney(1)
            if (itemStack != null) {
                player.inventory.addItem(me.func.commons.gold)
                if (itemStack.getAmount() == 10 && user.role != Role.DETECTIVE) {
                    player.inventory.remove(Material.GOLD_INGOT)
                    player.inventory.setItem(if (user.role == Role.MURDER) 2 else 1, bow)
                    if (player.inventory.contains(Material.ARROW)) player.inventory.addItem(me.func.commons.arrow)
                    else player.inventory.setItem(20, me.func.commons.arrow)
                }
            } else player.inventory.setItem(8, me.func.commons.gold)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            item.remove()
            isCancelled = true
        }
    }

    private fun setupInteractListeners() {
        context.on<PlayerInteractEvent> {
            if (activeStatus != Status.GAME || hand == EquipmentSlot.OFF_HAND) return@on
            // todo map
            map.interactive.filterIsInstance<BlockInteract>().filter { it.trigger(this) }.forEach {
                val user = app.getUser(player)
                goldManager.take(user, it.gold) {
                    it.interact(user)
                    player.playSound(player.location, Sound.BLOCK_CLOTH_FALL, 1.1f, 1f)
                }
            }
        }

        val poisons = listOf(
            PotionEffect(PotionEffectType.BLINDNESS, 50, 1),
            PotionEffect(PotionEffectType.CONFUSION, 80, 1),
            PotionEffect(PotionEffectType.SLOW, 100, 1),
        )

        context.on<PlayerInteractAtEntityEvent> {
            // Механика шприца
            if (hand == EquipmentSlot.OFF_HAND) return@on
            val item = player.itemInHand
            if (clickedEntity is CraftPlayer && item != null && item.getType() == Material.CLAY_BALL) {
                val nmsIem = CraftItemStack.asNMSCopy(item)
                if (nmsIem.hasTag() && nmsIem.tag.hasKeyOfType("interact", 8)) {
                    player.itemInHand = null
                    player.sendMessage(Formatting.error("Вы одурманили ${clickedEntity.name}"))
                    (clickedEntity as CraftPlayer).addPotionEffects(poisons)
                    ModHelper.sendTitle(app.getUser(clickedEntity as CraftPlayer), "§cО нет! §aКиСлооТа")
                }
            }
        }
    }

    private fun setupConntecionListeners() {
        context.on<PlayerJoinEvent> {
            player.inventory.clear()
            player.gameMode = GameMode.ADVENTURE
            val user = app.getUser(player)

            user.stat.lastEnter = System.currentTimeMillis()

            // Заполнение имени для топа
            if (user.stat.lastSeenName == null || (user.stat.lastSeenName != null && user.stat.lastSeenName!!.isEmpty())) user.stat.lastSeenName =
                IAccountService.get().getNameByUuid(UUID.fromString(user.session.userId)).get(1, TimeUnit.SECONDS)

            if (activeStatus != Status.STARTING) return@on

            // Информация на моды, музыка
            context.after(5) {
                ModTransfer().string("§cМаньяк " + 2 * (1 + user.stat.villagerStreak) + "%")
                    .string("§bДетектив " + 3 * (1 + user.stat.villagerStreak) + "%")
                    .string(map.title)
                    .send("murder-join", user)

                Music.LOBBY.play(user)
            }
        }

        context.on<PlayerQuitEvent> {
            val user = app.getUser(player)

            user.stat.timePlayedTotal += System.currentTimeMillis() - user.stat.lastEnter

            MusicHelper.stop(user)

            player.scoreboard.teams.forEach { it.unregister() }

            if (activeStatus == Status.GAME && user.role == Role.VILLAGER) {
                user.stat.villagerStreak = 0
            }
        }
    }

    private fun setupInventoryListeners() {
        context.on<InventoryOpenEvent> {
            if (inventory.type == InventoryType.CHEST && activeStatus != Status.STARTING) isCancelled = true
        }
    }

    // DamageListeners
    private fun kill(victim: User, killer: User?) {
        val player = victim.player!!
        if (player.gameMode == GameMode.SPECTATOR) return
        if (victim.role == Role.DETECTIVE) killDetective(victim)
        if (victim.role == Role.MURDER) killMurder(victim)

        if (killer != null && Math.random() < 0.35) {
            game.broadcast(killer.stat.activeKillMessage.texted(victim.name))
        } else {
            game.broadcast(KillMessage.NONE.texted(victim.name))
        }

        ModHelper.sendTitle(victim, "Вы проиграли")

        app.getUser(player).sendPlayAgain("§cСмерть!", map)

        player.gameMode = GameMode.SPECTATOR
        player.inventory.clear()
        victim.role = Role.NONE
        LocalModHelper.updateOnline()

        var location = player.location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 20)

        if (victim.stat.activeCorpse != Corpse.NONE) StandHelper(location.clone().subtract(0.0, 1.5, 0.0)).marker(true)
            .invisible(true)
            .gravity(false)
            .slot(EnumItemSlot.HEAD, victim.stat.activeCorpse.getIcon())
            .markTrash()
        else ModHelper.makeCorpse(victim)

        game.players.forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, 1f, 1f)
        }
    }

    private fun killDetective(user: User) {
        // Сообщение о выпадении лука
        ModHelper.sendGlobalTitle("§cЛук выпал")
        droppedBowManager.drop(user.player!!.location)
    }

    private fun killMurder(user: User) {
        // Детектив/Мирный житель убивает с лука убийцу
        game.players.minus(user.player!!).forEach {
            winMessage = "§aМирные жители победили!"
            ModHelper.sendTitle(app.getUser(it), "Победа мирных")
        }
        activeStatus = Status.END
    }
    // DamageListeners
}
