package me.func.commons.map

import clepto.bukkit.B
import me.func.commons.map.interactive.BlockInteract
import me.func.commons.map.interactive.Interactive
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.math.V3
import ru.cristalix.core.util.UtilEntity

enum class MapType(val title: String, val realmMod: Int, val address: String, val npcSkin: String, val data: MapData, val interactive: List<Interactive<out PlayerEvent>>) {
    OUTLAST(
        "Аутласт", 2, "hall", "6f3f4a2e-7f84-11e9-8374-1cb72caa35fd", MapData(
            "OUTLAST",
            43.0, -16.0,
            "mcpatcher/cit/others/colors/a.png",
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
                        StandardsInteract.dropSomething(drop, dropShelf, stableShelf, Material.BOOKSHELF, Material.AIR)
                    B.postpone(20 * 30) {
                        drop = false
                        StandardsInteract.dropSomething(drop, dropShelf, stableShelf, Material.AIR, Material.BOOKSHELF)
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
    ), FIELD("Ферма", 3, "field", "303c1f40-2c69-11e8-b5ea-1cb72caa35fd", MapData(
        "FIELD",
        43.0, -16.0,
        "mcpatcher/cit/others/colors/a.png",
        "1.png",
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