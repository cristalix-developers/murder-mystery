package me.func.murder.util

import clepto.bukkit.B
import me.func.commons.gold
import me.func.commons.user.User
import me.func.commons.worldMeta
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector

lateinit var goldManager: GoldManager

class GoldManager(var places: List<Location>) {

    init {
        goldManager = this
    }

    private val spawned = arrayListOf<Location>()
    private val vector = Vector(0.0, 0.4, 0.0)

    fun dropGoldRandomly() {
        val any = places.minus(spawned).filter { it -> it.getNearbyEntities(4.0,4.0,4.0).map { it.type }.isEmpty() }

        if (any.isEmpty())
            return

        val randomLocation = any.random()

        // Генерация золота и подбрасывание его вверх, куодаун 20 секунд
        dropGold(randomLocation)
        spawned.add(randomLocation)
        B.postpone(20 * 20) { spawned.remove(randomLocation) }
    }

    private fun forceTake(user: User, count: Int) {
        val newGold = gold.clone()
        newGold.setAmount(count)
        user.player!!.inventory.removeItem(newGold)
        user.player!!.updateInventory()
    }

    private fun has(user: User, count: Int): Boolean {
        return user.player!!.inventory.contains(Material.GOLD_INGOT, count)
    }

    fun take(user: User, count: Int, ifPresent: () -> Any) {
        if (has(user, count)) {
            forceTake(user, count)
            ifPresent()
        }
    }

    fun dropGold(location: Location) {
        worldMeta.world.dropItemNaturally(location, gold).velocity = vector
    }
}
