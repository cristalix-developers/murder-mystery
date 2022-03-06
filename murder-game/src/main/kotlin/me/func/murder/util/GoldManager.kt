package me.func.murder.util

import me.func.murder.MurderGame
import me.func.murder.user.User
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector

class GoldManager(private val game: MurderGame) {

    private val places: MutableList<Location> = mutableListOf()
    private val spawned = arrayListOf<Location>()
    private val velocity = Vector(0.0, 0.4, 0.0)

    fun dropGoldRandomly() {
        if (places.isEmpty()) places.addAll(game.map.getLabels("gold").map { it.toCenterLocation() })

        val any = places.minus(spawned.toSet()).filter { it ->
            it.getNearbyEntities(4.0, 4.0, 4.0).map { it.type }.isEmpty()
        }

        if (any.isEmpty()) return

        val randomLocation = any.random()

        // Генерация золота и подбрасывание его вверх, кулдаун 25 секунд
        dropGold(randomLocation)
        spawned.add(randomLocation)
        game.context.after(20 * 25) { spawned.remove(randomLocation) }
    }

    private fun forceTake(user: User, count: Int) {
        val newGold = MurderGame.gold.clone()
        newGold.setAmount(count)
        user.player.inventory.removeItem(newGold)
        user.player.updateInventory()
    }

    private fun has(user: User, count: Int): Boolean {
        return user.player.inventory.contains(Material.GOLD_INGOT, count)
    }

    fun take(user: User, count: Int, ifPresent: () -> Any) {
        if (has(user, count)) {
            forceTake(user, count)
            ifPresent()
        }
    }

    private fun dropGold(location: Location) {
        game.map.world.dropItemNaturally(location, MurderGame.gold).velocity = velocity
    }
}
