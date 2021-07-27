package me.func.murder.map

import clepto.bukkit.B
import me.func.murder.interactive.BlockInteract
import me.func.murder.interactive.Interactive
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.math.V3

enum class MapType(val title: String, val address: String, val data: MapData, val interactive: List<Interactive<out PlayerEvent>>) {
    OUTLAST(
        "Аутласт", "hall", MapData(
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

                override fun interact(user: User) {
                    StandardsInteract.movePlayer(user, inDot, outDot, 8 * 20, V3(-3.0, 110.0, -61.0))
                }
            }, object : BlockInteract(V3(-3.0, 117.0, -61.0), 2, "Подняться") {
                val inDot = V3(-2.0, 116.0, -65.0)
                val outDot = V3(-2.0, 126.0, -65.0)

                override fun interact(user: User) {
                    StandardsInteract.movePlayer(user, inDot, outDot, 5 * 20, V3(-3.0, 126.0, -61.0))
                }
            }, object : BlockInteract(V3(-3.0, 110.0, -61.0), 2, "Подняться") {
                val inDot = V3(-2.0, 109.0, -65.0)
                val outDot = V3(-2.0, 116.0, -65.0)

                override fun interact(user: User) {
                    StandardsInteract.movePlayer(user, inDot, outDot, 5 * 20, V3(-3.0, 116.0, -61.0))
                }
            }, object : BlockInteract(V3(24.0, 117.0, -65.0), 8, "Повредить энергопередачу") {
                override fun interact(user: User) {
                    StandardsInteract.breakLamps()
                }
            })
    )
}