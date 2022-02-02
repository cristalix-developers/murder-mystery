package me.func.murder.donate.impl

import me.func.murder.donate.DonatePosition
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.inventory.ItemStack

enum class DeathImage : DonatePosition {
    NONE;

    override val price: Int get() = TODO("Not yet implemented")
    override val icon: ItemStack get() = TODO("Not yet implemented")
    override val rare: Rare get() = TODO("Not yet implemented")
    override val title: String get() = TODO("Not yet implemented")

    override fun give(user: User) = TODO("Not yet implemented")
    override fun isActive(user: User) = TODO("Not yet implemented")
}
