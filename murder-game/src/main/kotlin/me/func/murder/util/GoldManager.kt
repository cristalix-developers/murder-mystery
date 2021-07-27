package me.func.murder.util

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.murder.worldMeta
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

lateinit var goldManager: GoldManager

val gold: ItemStack = item {
    type = Material.GOLD_INGOT
    text("§eЗолото\n\n§7Соберите §e10 штук§7,\n§7и получите §bлук§7!\n§7Или покупайте действия\n§7на карте.")
}.build()

class GoldManager(private val places: List<Location>) {

    init {
        goldManager = this
    }

    private val spawned = arrayListOf<Location>()

    fun dropGoldRandomly() {
        val any = places.minus(spawned).filter { location ->
            location.getNearbyEntities(4.0,4.0,4.0).map { it.type }.isEmpty()
        }

        if (any.isEmpty())
            return

        val randomLocation = any.random()

        // Генерация золота и подбрасывание его вверх, куодаун 20 секунд
        GoldDropper.dropGold(randomLocation)
        spawned.add(randomLocation)
        B.postpone(20 * 20) { spawned.remove(randomLocation) }
    }

}

object GoldDropper {
    private val vector = Vector(0.0, 0.4, 0.0)

    fun dropGold(location: Location) {
        worldMeta.world.dropItemNaturally(location, gold).velocity = vector
    }
}