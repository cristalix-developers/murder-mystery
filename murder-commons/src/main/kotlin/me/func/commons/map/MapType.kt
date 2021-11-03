package me.func.commons.map

import clepto.bukkit.B
import clepto.bukkit.Cycle
import dev.implario.bukkit.item.item
import me.func.commons.map.interactive.BlockInteract
import me.func.commons.map.interactive.Interactive
import me.func.commons.mod.ModHelper
import me.func.commons.user.User
import me.func.commons.util.Music
import me.func.commons.util.StandHelper
import me.func.commons.worldMeta
import net.minecraft.server.v1_12_R1.EnumItemSlot.HEAD
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.math.V3
import ru.cristalix.core.util.UtilEntity
import ru.cristalix.core.util.UtilV3

enum class MapType(
    val title: String,
    val realmMod: Int,
    val music: Music,
    val address: String,
    val npcSkin: String,
    val data: MapData,
    val interactive: List<Interactive<out PlayerEvent>>
) {
    OUTLAST(
        "Аутласт", 2, Music.OUTLAST, "hall", "6f3f4a2e-7f84-11e9-8374-1cb72caa35fd", MapData(
            "OUTLAST",
            43.0, -16.0,
            "mcpatcher/cit/others/mod items/gem_ruby.png",
            "1.png",
            128.0,
            arrayListOf()
        ), arrayListOf(
            object : BlockInteract(V3(37.0, 117.0, -46.0), 1, "Сдвинуть дверь") {
                override fun interact(user: User) {
                    StandardsInteract.closeDoor(V3(36.0, 116.0, -48.0), 100)
                }
            }, object : BlockInteract(V3(-43.0, 117.0, -46.0), 1, "Сдвинуть дверь") {
                override fun interact(user: User) {
                    StandardsInteract.closeDoor(V3(-42.0, 116.0, -47.0), 100)
                }
            }, object : BlockInteract(V3(13.0, 126.0, -34.0), 3, "Уронить полку") {
                var drop = false

                val dropShelf = listOf(
                    V3(14.0, 125.0, -33.0),
                    V3(13.0, 125.0, -33.0),
                    V3(14.0, 126.0, -33.0),
                    V3(14.0, 125.0, -34.0),
                    V3(13.0, 125.0, -34.0),
                    V3(14.0, 125.0, -35.0),
                    V3(13.0, 125.0, -35.0),
                    V3(14.0, 126.0, -34.0),
                )

                val stableShelf = listOf(
                    V3(14.0, 127.0, -32.0),
                    V3(14.0, 128.0, -32.0),
                    V3(14.0, 127.0, -32.0),
                    V3(14.0, 128.0, -32.0),
                    V3(13.0, 127.0, -32.0),
                    V3(13.0, 128.0, -32.0),
                )

                override fun trigger(event: PlayerInteractEvent): Boolean {
                    return super.trigger(event) && !drop
                }

                override fun interact(user: User) {
                    drop =
                        StandardsInteract.dropAndExplode(drop, dropShelf, stableShelf, Material.BOOKSHELF, Material.AIR)
                    B.postpone(20 * 30) {
                        drop = false
                        StandardsInteract.dropAndExplode(drop, dropShelf, stableShelf, Material.AIR, Material.BOOKSHELF)
                    }
                }
            }, object : BlockInteract(V3(-3.0, 126.0, -61.0), 1, "Спуститься") {
                val inDot = V3(-2.0, 126.0, -65.0)
                val outDot = V3(-2.0, 109.0, -65.0)

                override fun trigger(event: PlayerInteractEvent): Boolean {
                    return super.trigger(event) && !event.player.isInsideVehicle
                }

                override fun interact(user: User) {
                    StandardsInteract.movePlayer(user, inDot, outDot, 8 * 20, V3(-3.0, 110.0, -61.0))
                }
            }, object : BlockInteract(V3(-3.0, 117.0, -61.0), 2, "Подняться") {
                val inDot = V3(-2.0, 116.0, -65.0)
                val outDot = V3(-2.0, 126.0, -65.0)

                override fun trigger(event: PlayerInteractEvent): Boolean {
                    return super.trigger(event) && !event.player.isInsideVehicle
                }

                override fun interact(user: User) {
                    StandardsInteract.movePlayer(user, inDot, outDot, 5 * 20, V3(-3.0, 126.0, -61.0))
                }
            }, object : BlockInteract(V3(-3.0, 110.0, -61.0), 2, "Подняться") {
                val inDot = V3(-2.0, 109.0, -65.0)
                val outDot = V3(-2.0, 116.0, -65.0)

                override fun trigger(event: PlayerInteractEvent): Boolean {
                    return super.trigger(event) && !event.player.isInsideVehicle
                }

                override fun interact(user: User) {
                    StandardsInteract.movePlayer(user, inDot, outDot, 5 * 20, V3(-3.0, 116.0, -61.0))
                }
            }, object : BlockInteract(V3(24.0, 117.0, -65.0), 8, "Повредить энергопередачу") {
                override fun interact(user: User) {
                    StandardsInteract.breakLamps()
                }
            })
    ),
    FIELD(
        "Ферма НЛО", 3, Music.FIELD, "field", "303c1f40-2c69-11e8-b5ea-1cb72caa35fd", MapData(
            "FIELD",
            43.0, 71.0,
            "mcpatcher/cit/others/mod items/gem_ruby.png",
            "field.png",
            256.0,
            arrayListOf()
        ), listOf(object : BlockInteract(V3(-26.0, 92.0, 8.0), 4, "Взять шприц") {
            val syringe = item {
                type = Material.CLAY_BALL
                text("§dКислотный шприц\n\n§7Примените его на игроке\n§7и ему станет плохо.")
                nbt("murder", "shpric")
                nbt("interact", "shpric")
            }.build()

            override fun trigger(event: PlayerInteractEvent): Boolean {
                return super.trigger(event) && !event.player.inventory.contains(syringe)
            }

            override fun interact(user: User) {
                user.player!!.inventory.setItem(3, syringe)
            }
        }, object : BlockInteract(V3(1.0, 101.0, -7.0), 3, "Сломать мост") {
            var broken = false
            val breakList = listOf(
                V3(2.0, 100.0, -7.0),
                V3(1.0, 100.0, -7.0),
                V3(1.0, 100.0, -6.0),
                V3(0.0, 100.0, -6.0),
                V3(0.0, 100.0, -7.0),
                V3(-1.0, 100.0, -7.0)
            )
            var setBack = mutableListOf<Pair<Int, Byte>>()

            override fun trigger(event: PlayerInteractEvent): Boolean {
                return super.trigger(event) && !broken
            }

            override fun interact(user: User) {
                broken = true
                setBack = StandardsInteract.drop(breakList)
                // Когда игра закончится вернуть все как было
                Cycle.run(20 * 30, 25) {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        broken = false
                        breakList.map { UtilV3.toLocation(it, worldMeta.world) }
                            .forEachIndexed { index, location ->
                                val currentBlock = setBack[index]
                                location.block.setTypeAndDataFast(currentBlock.first, currentBlock.second)
                                location.set(location.x, 91.0, location.z).block.setTypeAndDataFast(0, 0)
                            }
                        setBack.clear()
                        Cycle.exit()
                    }
                }
            }
        }, object : BlockInteract(V3(-9.0, 91.0, 18.0), 9, "Призыв пришельцев") {
            var isActive = false
            val spawn = V3(-9.0, 120.0, 18.0)

            override fun trigger(event: PlayerInteractEvent): Boolean {
                return super.trigger(event) && !isActive
            }

            override fun interact(user: User) {
                isActive = true

                val commander = StandHelper(UtilV3.toLocation(spawn, worldMeta.world))
                    .gravity(false)
                    .slot(HEAD, ItemStack(Material.IRON_BLOCK))
                    .marker(true)
                    .invisible(true)
                    .markTrash()
                    .build()
                UtilEntity.setScale(commander, 4.0, 4.0, 4.0)
                val children = mutableListOf<ArmorStand>()
                val amount = 9
                val radius = 2.7
                val center = commander.location.clone()

                repeat(amount) {
                    val radians = Math.toRadians(360.0 / amount * it)
                    val currentLocation =
                        center.clone().add(kotlin.math.sin(radians) * radius, -.7, kotlin.math.cos(radians) * radius)

                    val stand = StandHelper(currentLocation)
                        .gravity(false)
                        .slot(
                            HEAD,
                            ItemStack(Material.IRON_PLATE)
                        )
                        .marker(true)
                        .invisible(true)
                        .markTrash()
                        .build()
                    val pose = stand.headPose
                    pose.y = -radians
                    stand.headPose = pose
                    children.add(stand)
                }
                children.forEach { UtilEntity.setScale(it, 5.0, 5.0, 5.0) }

                val victim = Bukkit.getOnlinePlayers()
                    .filter { it.gameMode != org.bukkit.GameMode.SPECTATOR }
                    .random()
                var isKilling = false

                ModHelper.sendGlobalTitle("㥗 §aПришествие!")

                val speed = 14

                Cycle.run(1, 20 * 30) {
                    if (victim.gameMode == org.bukkit.GameMode.SPECTATOR) {
                        isKilling = false
                        isActive = false
                        clepto.bukkit.Cycle.exit()
                    }

                    val pose = commander.headPose
                    pose.y += Math.toRadians(7.0)
                    commander.headPose = pose

                    if (!isKilling) {
                        (commander as org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand).handle.move(
                            net.minecraft.server.v1_12_R1.EnumMoveType.SELF,
                            (victim.location.x - commander.location.x) / speed,
                            (victim.location.y - commander.location.y + 4.5) / speed,
                            (victim.location.z - commander.location.z) / speed
                        )

                        children.forEachIndexed { index, stand ->
                            val rad = Math.toRadians(360.0 / amount * index)
                            (stand as org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand).handle.move(
                                net.minecraft.server.v1_12_R1.EnumMoveType.SELF,
                                (victim.location.x - stand.location.x - kotlin.math.sin(rad) * radius) / speed,
                                (victim.location.y - stand.location.y + 4.3) / speed,
                                (victim.location.z - stand.location.z - kotlin.math.cos(rad) * radius) / speed
                            )
                            val standPose = stand.headPose
                            standPose.y = -rad
                            stand.headPose = standPose
                        }
                    }

                    if (isActive && !isKilling && commander.location.distanceSquared(victim.location) < 23) {
                        isKilling = true
                        victim.addPotionEffect(
                            org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.LEVITATION,
                                9 * 20,
                                0
                            )
                        )
                        victim.location.pitch = -90f

                        repeat(300) {
                            worldMeta.world.spawnParticle(
                                org.bukkit.Particle.VILLAGER_HAPPY,
                                commander.location.x - kotlin.math.sin(Math.toRadians(it * 10.0)) * it / 120.0,
                                commander.location.y - it / 35.0 + 2.9,
                                commander.location.z - kotlin.math.cos(Math.toRadians(it * 10.0)) * it / 120.0,
                                1
                            )
                        }

                        B.postpone(8 * 20) {
                            victim.damage(0.0051)
                            isKilling = false
                            isActive = false
                        }
                    }
                }
            }
        })
    ),
    PORT(
        "Порт", 4, Music.PORT, "Port", "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        MapData(
            "PORT",
            107.0, 69.0,
            "mcpatcher/cit/others/mod items/gem_ruby.png",
            "port.png",
            256.0,
            arrayListOf()
        ), listOf(object : BlockInteract(V3(77.5, 91.0, -9.5), 4, "Сломать мост") {
            var broken = false
            val breakList = listOf(
                V3(78.0, 89.0, -9.0),
                V3(77.0, 89.0, -9.0),
                V3(76.0, 89.0, -9.0),
                V3(78.0, 89.0, -10.0),
                V3(77.0, 89.0, -10.0),
                V3(76.0, 89.0, -10.0),
            )
            var setBack = mutableListOf<Pair<Int, Byte>>()

            override fun trigger(event: PlayerInteractEvent): Boolean {
                return super.trigger(event) && !broken
            }

            override fun interact(user: User) {
                broken = true
                setBack = StandardsInteract.drop(breakList)
                // Когда игра закончится вернуть все как было
                Cycle.run(20 * 30, 25) {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        broken = false
                        breakList.map { UtilV3.toLocation(it, worldMeta.world) }
                            .forEachIndexed { index, location ->
                                val currentBlock = setBack[index]
                                location.block.setTypeAndDataFast(currentBlock.first, currentBlock.second)
                            }
                        setBack.clear()
                        Cycle.exit()
                    }
                }
            }
        })
    ),
    DBD(
        "Dead By Daylight", 7, Music.LOBBY, "dbd", "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        MapData(
            "DBD",
            36.0, 14.0,
            "mcpatcher/cit/others/mod items/gem_ruby.png",
            "dbd.png",
            128.0,
            arrayListOf()
        ), listOf()
    ),
    DBD2(
        "Dead By Daylight", 8, Music.LOBBY, "dbd2", "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        MapData(
            "DBD2",
            41.0, 73.0,
            "mcpatcher/cit/others/mod items/gem_ruby.png",
            "dbd2.png",
            128.0,
            arrayListOf()
        ), listOf());

    fun loadDetails(entities: Array<Entity>) {
        entities.filterIsInstance<ArmorStand>()
            .filter { it.helmet != null && it.helmet.getType() == Material.CLAY_BALL }
            .forEach {
                val type = CraftItemStack.asNMSCopy(it.helmet)
                if (type.hasTag() && type.tag.hasKeyOfType("murder", 8)) {
                    when (type.tag.getString("murder")) {
                        "kreslo" -> UtilEntity.setScale(it, 1.2, 1.4, 1.2)
                        "divan" -> UtilEntity.setScale(it, 1.2, 1.4, 1.4)
                    }
                }
            }
    }
}