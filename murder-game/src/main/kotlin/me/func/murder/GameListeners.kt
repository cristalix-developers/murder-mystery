@file:Suppress("DEPRECATION", "DuplicatedCode")

package me.func.murder

import clepto.bukkit.Cycle
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import me.func.Arcade
import me.func.battlepass.BattlePassUtil
import me.func.battlepass.quest.QuestType
import me.func.donate.impl.Corpse
import me.func.donate.impl.KillMessage
import me.func.mod.Anime
import me.func.mod.conversation.ModLoader
import me.func.mod.conversation.ModTransfer
import me.func.murder.dbd.DbdStatus
import me.func.murder.dbd.mechanic.GadgetMechanic
import me.func.murder.dbd.mechanic.drop.ChestManager
import me.func.murder.map.interactive.BlockInteract
import me.func.murder.mod.ModHelper
import me.func.murder.user.Role
import me.func.murder.user.User
import me.func.murder.util.Music
import me.func.murder.util.MusicHelper
import me.func.murder.util.StandHelper
import me.func.protocol.Indicators
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import org.spigotmc.event.entity.EntityDismountEvent
import ru.cristalix.core.formatting.Formatting
import java.awt.SystemColor.text
import java.util.UUID

/**
 * Created by Kamillaova on 2022.01.30.
 */
class GameListeners(private val game: MurderGame, dbd: Boolean) {
    private val context = game.context
    private val playersInGame: MutableList<String> = arrayListOf()

    init {
        context.after(1) {
            if (dbd) {
                setupDbdJoinListeners()
                setupDeathHandlerListeners()
                setupBlockPhysicsCancelListeners()
                ChestManager(game)
                setupItemHolderListeners()
                setupMoveHandlerListeners()
            } else {
                setupDamageListeners()
                setupConnectionListeners()
                setupGoldListeners()
                setupChatListeners()
                setupInteractListeners()
                setupInventoryListeners()
            }
            setupMapDecorationListeners()
            setupGlobalListeners()
        }
    }

    // DBD
    private fun setupDbdJoinListeners() {
        context.on<PlayerJoinEvent> {
            player.inventory.clear()
            player.gameMode = GameMode.ADVENTURE
            val user = game.userManager.getUser(player)

            user.stat.lastEnter = System.currentTimeMillis()

            if (game.activeDbdStatus == DbdStatus.STARTING) {
                // Информация на моды, музыка
                context.after(5) {
                    ModTransfer().string("§cМаньяк 20%")
                        .string("§aЖертва 80%")
                        .string(game.mapType.title)
                        .send("murder-join", player)

                    Music.LOBBY.play(user)
                }
            }

            // Заполнение имени для топа
            if (user.stat.lastSeenName.isEmpty()) user.stat.lastSeenName =
                game.cristalix.getPlayer(player)?.displayName ?: player.displayName
        }
    }

    private fun setupItemHolderListeners() {
        context.on<PlayerPickupItemEvent> {
            if (game.killer?.player!! != player) {
                if (item.itemStack == ChestManager.fuel) {
                    if (player.inventory.getItem(2) == null) item.remove()
                    player.inventory.setItem(2, ChestManager.fuel)
                } else if (item.itemStack == GadgetMechanic.bandage) {
                    if (player.inventory.getItem(3) == null) item.remove()
                    player.inventory.setItem(3, GadgetMechanic.bandage)
                }
            }
            isCancelled = true
        }
    }

    private val winZone = game.map.getLabels("win")

    private fun setupMoveHandlerListeners() {
        context.on<InventoryOpenEvent> { if (inventory.type == InventoryType.ENCHANTING) cancelled = true }
        context.on<PlayerBedEnterEvent> { cancel = true }

        context.on<PlayerMoveEvent> {
            if (game.activeDbdStatus == DbdStatus.GAME && player == game.killer?.player && player.velocity.y > 0 && !player.isOnGround) {
                isCancelled = true
                cancel = true
            }

            if (to.blockX == from.blockX && to.blockY == from.blockY && to.blockZ == from.blockZ && player == game.killer?.player) return@on
            if (game.activeDbdStatus == DbdStatus.GAME && player != game.killer!!.player && player.gameMode != GameMode.SPECTATOR) {
                game.gadgetMechanic.traps.removeIf {
                    if (it.location.distanceSquared(player.location) < 3.5) {
                        it.helmet = GadgetMechanic.closeTrap
                        player.addPotionEffect(GadgetMechanic.slowness)
                        Music.DBD_RUN.playAll(game)
                        context.after(20 * 5) { Music.DBD_GAME.playAll(game) }
                        return@removeIf true
                    }
                    return@removeIf false
                }
                winZone.filter { to.distanceSquared(it) < it.tagInt * it.tagInt }.forEach { _ ->
                    val user = game.userManager.getUser(player)
                    if (user.role == Role.VICTIM && player.gameMode != GameMode.SPECTATOR) {
                        BattlePassUtil.update(user.player, QuestType.WIN, 1, false)
                        user.stat.wins++
                        user.role = Role.NONE
                        user.out = true
                        Arcade.deposit(player, 3)

                        player.gameMode = GameMode.SPECTATOR
                        player.velocity = player.velocity.multiply(1.1).add(Vector(0.0, 1.6, 0.0))
                        game.broadcast("  > §e${player.name} §aвыбрался наружу и убежал!")
                    } else if (player == game.killer?.player) {
                        isCancelled = true
                        cancel = true
                    }
                }
                game.engineManager.engines.filter { it.key.percent < 100 && it.key.location.distanceSquared(to) < 18 }
                    .forEach { (_, _) ->
                        player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR, TextComponent("§eЗалейте топливо! §f§lПКМ §eпо двигателю")
                        )
                    }
            }
        }
    }

    private val speed = PotionEffect(PotionEffectType.SPEED, 20 * 6, 3)

    private fun setupDeathHandlerListeners() {
        context.on<EntityDamageByEntityEvent> {
            if (entity is CraftPlayer && damager == game.killer?.player && !(entity as CraftPlayer).hasPotionEffect(
                    PotionEffectType.SPEED
                ) && !(entity as CraftPlayer).hasPotionEffect(PotionEffectType.INVISIBILITY)
            ) {
                val victim = game.userManager.getUser(entity as CraftPlayer)

                game.killer?.let { game.killer!!.bites++ } ?: return@on

                val respawnTimeSeconds = 20

                if (victim.hearts > 1) {
                    game.after(4 * 20) { game.killer!!.player.inventory.setItem(3, GadgetMechanic.openTrap) }
                    victim.hearts--
                    (entity as CraftPlayer).addPotionEffect(speed)
                    Music.DBD_RUN.playAll(game)
                    game.after(20L * respawnTimeSeconds) { Music.DBD_GAME.playAll(game) }
                } else {
                    val player = victim.player

                    // Если игрока еще можно спасти
                    if (game.players.filter { game.userManager.getUser(it).role == Role.VICTIM }.size > 1) {
                        game.modHelper.makeCorpse(player)
                        victim.player.gameMode = GameMode.SPECTATOR

                        Anime.title(victim.player, "§cВас ранили!\n§eЖдите помощи")
                        Anime.reload(victim.player, respawnTimeSeconds.toDouble(), "Вас еще могут спасти!")
                        game.broadcast(
                            "  §l> §cИгрок §e${player.name} §cпал! Чтобы спасти нажмите §f§lSHIFT §c c §e1 бинтом§c! Осталось 15 секунд."
                        )
                        game.players.filter { it != game.killer!!.player }.forEach {
                            ModTransfer().string(player.uniqueId.toString())
                                .double(player.location.x)
                                .double(player.location.y + 1.0)
                                .double(player.location.z)
                                .string("textures/others/znak_v_3.png")
                                .send("holo", it)
                        }


                        val location = victim.player.location.clone().add(0.0, 1.3, 0.0)
                        location.pitch = 90f

                        Cycle.run(1, 20 * respawnTimeSeconds) { time ->
                            if (time == 20 * respawnTimeSeconds - 1) {
                                dbdKill(victim)
                                return@run
                            }

                            victim.player.teleport(location)

                            game.players.filter {
                                it.location.distanceSquared(location) < 12 && it != game.killer!!.player && it
                                    .gameMode != GameMode.SPECTATOR
                            }.forEach {
                                if (!it.inventory.contains(GadgetMechanic.bandage)) {
                                    it.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR, TextComponent("§cВам нужен §e§l1 бинт§c!")
                                    )
                                } else if (!it.isSneaking) {
                                    it.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR, TextComponent("§cНажмите §e§lSHIFT§c, чтобы спасти")
                                    )
                                } else {
                                    it.inventory.setItem(3, null)

                                    it.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§l-1 §fбинт"))

                                    val uuid = victim.player.uniqueId.toString()

                                    // Отправляем труп опять, чтобы удалить
                                    game.players.forEach { player ->
                                        game.modHelper.sendCorpse(
                                            victim.player.name, victim.player.uniqueId, player, 0.0, 0.0, 0.0
                                        )
                                        ModTransfer().string(uuid).send("holohide", player.player!!)
                                    }

                                    victim.hearts = 1
                                    victim.player.gameMode = GameMode.ADVENTURE
                                    victim.player.addPotionEffect(
                                        PotionEffect(
                                            PotionEffectType.CONFUSION, 20 * 2, 1
                                        )
                                    )
                                    victim.player.addPotionEffect(speed)

                                    game.broadcast("  §l> §e${victim.player.name} §aспасен благодаря помощи  §e${it.name}")
                                    Anime.title(victim.player, "§cВас ранили!\n§eЖдите помощи")
                                    Cycle.exit()
                                }
                            }
                        }
                    } else {
                        dbdKill(victim)
                    }
                }
            } else {
                isCancelled = true
            }
        }

        context.on<EntityDamageEvent> { if (game.activeDbdStatus != DbdStatus.GAME) cancelled = true }
        context.on<PlayerQuitEvent> {
            if (game.activeDbdStatus == DbdStatus.GAME) dbdKill(
                game.userManager.getUser(player)
            )
        }
    }

    private fun setupBlockPhysicsCancelListeners() {
        context.on<BlockPhysicsEvent> {
            cancel = true
            isCancelled = true
        }
        context.on<PlayerToggleSprintEvent> { isSprinting = false }
    }

    private fun dbdKill(victim: User) {
        if (victim.role == Role.VICTIM) {
            val uuid = victim.player.uniqueId.toString()

            game.players.filter { it != (game.killer?.player ?: return@dbdKill) }.forEach { // ok
                ModTransfer().string(uuid).send("holohide", it)
            }

            if (game.killer != null && Math.random() < 0.35) {
                game.broadcast("  > " + Arcade.getArcadeData(game.killer!!.stat.id).killMessage.texted(victim.name))
            } else {
                game.broadcast("  > " + KillMessage.NONE.texted(victim.name))
            }
            Anime.title(victim.player, "Вас убили!")
            BattlePassUtil.update(game.killer?.player!!, QuestType.KILL, 1, false)
            game.killer!!.stat.eventKills++
            Arcade.deposit(game.killer!!.player.uniqueId, 1)

            val corpse = Arcade.getArcadeData(victim.stat.id).corpse

            val location = victim.player.location.clone()
            if (corpse != Corpse.NONE) StandHelper(location.clone().subtract(0.0, 1.5, 0.0)).marker(true)
                .invisible(true)
                .gravity(false)
                .slot(EnumItemSlot.HEAD, corpse.getIcon())
                .markTrash()

            Music.DBD_DEATH.playAll(game)
            game.context.after(5 * 20) { Music.DBD_GAME.playAll(game) }
        }
        victim.hearts = 2
        victim.player.gameMode = GameMode.SPECTATOR
        victim.player.inventory.clear()
        victim.role = Role.NONE
        game.modHelper.updateOnline()

        game.players.forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, 1f, 1f)
        }
    }

    private fun setupGlobalListeners() {
        context.on<ProjectileHitEvent> { if (entity is Arrow) entity.remove() }
        context.on<BlockPlaceEvent> { isCancelled = true }
        context.on<BlockBreakEvent> { isCancelled = true }
        context.on<BlockRedstoneEvent> { newCurrent = oldCurrent }
        context.on<CraftItemEvent> { isCancelled = true }
        context.on<PlayerInteractEntityEvent> { isCancelled = true }
        context.on<PlayerDropItemEvent> { isCancelled = true }
        context.on<BlockFadeEvent> { isCancelled = true }
        context.on<BlockSpreadEvent> { isCancelled = true }
        context.on<BlockGrowEvent> { isCancelled = true }
        context.on<BlockFromToEvent> { isCancelled = true }
        context.on<HangingBreakByEntityEvent> { isCancelled = true }
        context.on<BlockBurnEvent> { isCancelled = true }
        context.on<EntityExplodeEvent> { isCancelled = true }
        context.on<PlayerArmorStandManipulateEvent> { isCancelled = true }
        context.on<PlayerAdvancementCriterionGrantEvent> { isCancelled = true }
        context.on<PlayerSwapHandItemsEvent> { isCancelled = true }
        context.on<InventoryClickEvent> { isCancelled = true }
        context.on<FoodLevelChangeEvent> { isCancelled = true }
        context.on<EntityDamageEvent> { if (cause == EntityDamageEvent.DamageCause.FALL) isCancelled = true }
        context.on<PlayerMoveEvent> {
            if (player.location.block.isLiquid && !game.dbd) kill(game.userManager.getUser(player))
        }

        context.on<PlayerJoinEvent> {
            game.context.after(3) {
                player.setResourcePack("", "")
                ModLoader.send("murder-mod-bundle.jar", player)
                Anime.hideIndicator(
                    player, Indicators.HUNGER, Indicators.EXP, Indicators.HEALTH, Indicators.VEHICLE, Indicators.TAB
                )

                Anime.loadTextures(
                    player,
                    *listOf(
                        "1.png",
                        "-1.png",
                        "2.png",
                        "field.png",
                        "port.png",
                        "dbd.png",
                        "dbd2.png"
                    ).map { "https://implario.dev/murder/${it}" }.toTypedArray()
                )
            }

            player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
            player.isGlowing = false
            player.foodLevel = 20
            player.health = 20.0
        }
    }

    private fun setupMapDecorationListeners() {
        context.on<PlayerMoveEvent> {
            if (to.distanceSquared(from) < 0.4) {
                player.getNearbyEntities(0.5, 0.5, 0.5).filter { it.hasMetadata("friend") }.forEach {
                    game.map.world.getEntity(UUID.fromString(it.getMetadata("friend")[0].asString()))
                        .teleport(it.location.clone().subtract(0.0, 1.0, 0.0))
                }
            }
        }

        context.on<EntityDismountEvent> {
            val toTeleport = dismounted.passengers[0]
            game.after(1) {
                toTeleport.teleport(dismounted.location.clone().add(0.0, 4.0, 0.0))
            }
        }

        context.on<PlayerInteractAtEntityEvent> {
            if (clickedEntity.type != EntityType.ARMOR_STAND) return@on
            val stand = clickedEntity as ArmorStand
            if (stand.helmet == null || stand.helmet.getType() != Material.CLAY_BALL || stand.passengers.size > 0) return@on
            val nmsItem = CraftItemStack.asNMSCopy(stand.helmet)
            if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("murder", 8)) {
                val tag = nmsItem.tag.getString("murder")
                if (tag == "kreslo" || tag == "divan" || tag == "katalka") {
                    stand.addPassenger(player)
                }
            }
        }
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

            if (game.activeStatus != Status.GAME) return@on

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
                val userVictim = game.userManager.getUser(victim)
                val userKiller = game.userManager.getUser(killer)

                if (userKiller == userVictim) {
                    if (byArrow) damager.remove()
                    return@on
                }

                if (userKiller.role == Role.MURDER) {
                    if (byArrow || killer.inventory.itemInMainHand.getType() == Material.IRON_SWORD || damage == 10.0) {
                        // Убийца убивает с меча или с лука
                        Arcade.deposit(userKiller.player.uniqueId, 2)
                        BattlePassUtil.update(userKiller.player, QuestType.KILL, 1, false)
                        userKiller.stat.kills++
                        kill(userVictim, userKiller)
                        val sword = killer.inventory.getItem(1)
                        killer.inventory.setItem(1, null)
                        ModHelper.sendCooldown(killer, "Возвращение орудия", 60)
                        context.after(50) { killer.inventory.setItem(1, sword) }
                        return@on
                    }
                } else if (byArrow) {
                    if (userVictim.role == Role.MURDER) {
                        game.heroName = userKiller.name
                        Arcade.deposit(userKiller.player.uniqueId, 3)
                    } else kill(userKiller, userKiller)
                    kill(userVictim, userKiller)
                } else return@on
            }
        }

        context.on<ProjectileLaunchEvent> {
            if (getEntity() is Arrow && getEntity().shooter is CraftPlayer) {
                (getEntity() as Arrow).pickupStatus = Arrow.PickupStatus.DISALLOWED
                val user = game.userManager.getUser(getEntity().shooter as Player)
                user.player.inventory.removeItem(MurderGame.arrow)
                if (user.role == Role.DETECTIVE) {
                    ModHelper.sendCooldown(user.player, "Перезарядка лука", 110)
                    context.after(100) { user.player.inventory.setItem(20, MurderGame.arrow) }
                }
            }
        }

        context.on<EntityDamageEvent> {
            isCancelled = true

            if (game.activeStatus == Status.GAME && damage < 0.01) {
                if (entity is CraftPlayer) {
                    kill(game.userManager.getUser(entity as CraftPlayer), null)
                }
            }
        }

        context.on<PlayerInteractEvent> {
            if (game.activeStatus != Status.GAME) return@on

            val user = game.userManager.getUser(player)

            // Если маньяк нажал на меч, то запустить его вперед
            if (user.role == Role.MURDER && action == Action.RIGHT_CLICK_AIR && material == Material.IRON_SWORD) {
                user.player.playSound(user.player.location, Sound.BLOCK_CLOTH_STEP, 1.1f, 1f)
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

                ModHelper.sendCooldown(user.player, "Возвращение орудия", giveBackTime + 10)
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
                    game.players.minus(player)
                        .filter { it.gameMode != GameMode.SPECTATOR && it.location.distanceSquared(sword.location) < 1.5 }
                        .forEach {
                            it.damage(10.0, player)
                        }
                }
            }
        }

        context.on<PlayerQuitEvent> {
            if (game.activeStatus != Status.GAME) return@on
            kill(game.userManager.getUser(player))
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
            val user = game.userManager.getUser(player.uniqueId)

            Arcade.deposit(player.uniqueId, 1)
            if (itemStack != null) {
                player.inventory.addItem(MurderGame.gold)
                if (itemStack.getAmount() == 10 && user.role != Role.DETECTIVE) {
                    player.inventory.remove(Material.GOLD_INGOT)
                    player.inventory.setItem(if (user.role == Role.MURDER) 2 else 1, bow)
                    if (player.inventory.contains(Material.ARROW)) player.inventory.addItem(MurderGame.arrow)
                    else player.inventory.setItem(20, MurderGame.arrow)
                }
            } else player.inventory.setItem(8, MurderGame.gold)
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            item.remove()
            isCancelled = true
        }
    }

    private fun setupInteractListeners() {
        context.on<PlayerInteractEvent> {
            if (game.activeStatus != Status.GAME || hand == EquipmentSlot.OFF_HAND) return@on
            game.mapType.interactive.filterIsInstance<BlockInteract>().filter { it.trigger(this) }.forEach {
                val user = game.userManager.getUser(player)
                game.goldManager.take(user, it.gold) {
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
                    Anime.title(clickedEntity as CraftPlayer, "§cО нет! §aКиСлооТа")
                }
            }
        }
    }

    private fun setupConnectionListeners() {
        context.on<PlayerJoinEvent> {
            playersInGame += player.name

            player.inventory.clear()
            player.gameMode = GameMode.ADVENTURE
            player.setResourcePack("", "")

            val user = game.userManager.getUser(player)

            if (game.activeStatus != Status.STARTING) return@on

            // Информация на моды, музыка
            context.after(5) {
                ModTransfer().string("§cМаньяк " + 2 * (1 + user.stat.villagerStreak) + "%")
                    .string("§bДетектив " + 3 * (1 + user.stat.villagerStreak) + "%")
                    .string(game.mapType.title)
                    .send("murder-join", player)

                Music.LOBBY.play(user)
            }

            // Заполнение имени для топа
            if (user.session.userId != null && user.stat.lastSeenName.isEmpty()) user.stat.lastSeenName =
                game.cristalix.getPlayer(UUID.fromString(user.session.userId))?.displayName ?: ("§7" + player.name)
        }

        context.on<PlayerQuitEvent> {
            playersInGame -= player.name

            val user = game.userManager.getUser(player)

            user.stat.timePlayedTotal += System.currentTimeMillis() - user.stat.lastEnter

            MusicHelper.stop(user)

            player.scoreboard.teams.forEach { it.unregister() }

            if (game.activeStatus == Status.GAME && user.role == Role.VILLAGER) {
                user.stat.villagerStreak = 0
            }
        }
    }

    private fun setupInventoryListeners() {
        context.on<InventoryOpenEvent> {
            if (inventory.type == InventoryType.CHEST && game.activeStatus != Status.STARTING) isCancelled = true
        }
    }

    // DamageListeners
    private fun kill(victim: User, killer: User? = null) {
        val player = victim.player
        if (player.gameMode == GameMode.SPECTATOR) return
        if (victim.role == Role.DETECTIVE) killDetective(victim)
        if (victim.role == Role.MURDER) killMurder(victim)

        if (killer != null && Math.random() < 0.35) {
            game.broadcast(Arcade.getArcadeData(killer.stat.id).killMessage.texted(victim.name))
        } else {
            game.broadcast(KillMessage.NONE.texted(victim.name))
        }

        Anime.title(player, "Вы проиграли")

        player.gameMode = GameMode.SPECTATOR
        player.inventory.clear()
        victim.role = Role.NONE
        game.modHelper.updateOnline()

        var location = player.location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 20)

        val corpse = Arcade.getArcadeData(victim.stat.id).corpse

        if (corpse != Corpse.NONE) StandHelper(location.clone().subtract(0.0, 1.5, 0.0)).marker(true)
            .invisible(true)
            .gravity(false)
            .slot(EnumItemSlot.HEAD, Arcade.getArcadeData(victim.stat.id).corpse.getIcon())
            .markTrash()
        else game.modHelper.makeCorpse(victim.player)

        game.players.forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_DEATH, 1f, 1f)
        }
    }

    private fun killDetective(user: User) {
        // Сообщение о выпадении лука
        game.modHelper.sendGlobalTitle("§cЛук выпал")
        game.bowManager.drop(user.player.location)
    }

    private fun killMurder(user: User) {
        // Детектив/Мирный житель убивает с лука убийцу
        game.players.minus(user.player).forEach {
            game.winMessage = "§aМирные жители победили!"
            Anime.title(it, "Победа мирных")
        }
        game.activeStatus = Status.END
    }
}
