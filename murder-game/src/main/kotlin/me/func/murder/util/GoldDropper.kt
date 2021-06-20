package me.func.murder.util

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.murder.app
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector

class GoldDropper(private val places: List<Location>) {

    private val spawned = arrayListOf<Location>()
    private val gold = item {
        type = Material.GOLD_INGOT
        text("Золото")
    }.build()
    private val vector = Vector(0.0, 0.4, 0.0)

    fun dropGold() {
        val any = places.minus(spawned).random()

        // Генерация золота и подбрасывание его вверх, куодаун 20 секунд
        app.worldMeta.world.dropItemNaturally(any, gold).velocity = vector
        spawned.add(any)

        B.postpone(20 * 20) { spawned.remove(any) }
    }

}