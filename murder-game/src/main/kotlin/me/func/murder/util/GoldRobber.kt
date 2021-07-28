package me.func.murder.util

import me.func.murder.user.User
import org.bukkit.Material
import ru.cristalix.core.item.Items

object GoldRobber {

    // Стак золотых слитков
    val stackOfGold = Items.fromStack(gold).amount(64).displayName("§eВаши монеты").build()

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
}