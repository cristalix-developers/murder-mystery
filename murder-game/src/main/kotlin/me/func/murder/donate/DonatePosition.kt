package me.func.murder.donate

import me.func.murder.user.User
import org.bukkit.inventory.ItemStack

interface DonatePosition {
    val name: String // may be overriden by enum ?
    val title: String
    val price: Int
    val rare: Rare
    val icon: ItemStack

    fun give(user: User)
    fun isActive(user: User): Boolean
}
