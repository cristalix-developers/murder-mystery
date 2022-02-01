package me.func.commons.donate

import me.func.commons.user.User
import org.bukkit.inventory.ItemStack

interface DonatePosition {
    val title: String
    val price: Int
    val rare: Rare
    val icon: ItemStack
    val name: String // may be overriden by enum ?

    fun give(user: User)
    fun isActive(user: User): Boolean
}
