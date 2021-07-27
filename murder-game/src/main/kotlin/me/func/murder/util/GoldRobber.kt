package me.func.murder.util

import me.func.murder.user.User
import org.bukkit.Material

object GoldRobber {
    private fun forceTake(user: User, count: Int) {
        val newGold = user.player!!.inventory.getItem(8).clone()
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
}