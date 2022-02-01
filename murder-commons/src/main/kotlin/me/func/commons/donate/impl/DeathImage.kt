package me.func.commons.donate.impl

import me.func.commons.donate.DonatePosition
import me.func.commons.donate.Rare
import me.func.commons.user.User
import org.bukkit.inventory.ItemStack

enum class DeathImage : DonatePosition {
    NONE;

    override val price: Int
        get() = TODO("Not yet implemented")

    override val icon: ItemStack
        get() = TODO("Not yet implemented")

    override val rare: Rare
        get() = TODO("Not yet implemented")

    override val title: String
        get() = TODO("Not yet implemented")

    override fun give(user: User) {
        TODO("Not yet implemented")
    }

    override fun isActive(user: User): Boolean {
        TODO("Not yet implemented")
    }
}