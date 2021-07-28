package me.func.murder.util

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.murder.user.User
import me.func.murder.worldMeta
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ru.cristalix.core.item.Items

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
    private val vector = Vector(0.0, 0.4, 0.0)
    // Стак золотых слитков
    val stackOfGold = Items.fromStack(gold).amount(64).displayName("§eВаши монеты").build()

    fun dropGoldRandomly() {
        val any = places.minus(spawned).filter { location ->
            location.getNearbyEntities(4.0,4.0,4.0).map { it.type }.isEmpty()
        }

        if (any.isEmpty())
            return

        val randomLocation = any.random()

        // Генерация золота и подбрасывание его вверх, куодаун 20 секунд
        dropGold(randomLocation)
        spawned.add(randomLocation)
        B.postpone(20 * 20) { spawned.remove(randomLocation) }
    }

    fun forceTake(user: User, count: Int, inGameGold: Boolean) {
        val newGold = if (inGameGold) gold.clone() else stackOfGold.clone()
        newGold.setAmount(count)
        user.player!!.inventory.removeItem(newGold)
        user.player!!.updateInventory()
    }

    private fun has(user: User, count: Int): Boolean {
        return user.player!!.inventory.contains(Material.GOLD_INGOT, count)
    }

    fun take(user: User, count: Int, ifPresent: () -> Any) {
        if (has(user, count)) {
            forceTake(user, count, true)
            ifPresent()
        }
    }

    fun dropGold(location: Location) {
        worldMeta.world.dropItemNaturally(location, gold).velocity = vector
    }
}
