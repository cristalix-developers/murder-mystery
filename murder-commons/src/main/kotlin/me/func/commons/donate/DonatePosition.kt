package me.func.commons.donate

import me.func.commons.user.User
import org.bukkit.inventory.ItemStack

interface DonatePosition {

    fun getTitle(): String

    fun getPrice(): Int

    fun getRare(): Rare

    fun getIcon(): ItemStack

    fun give(user: User)

    fun isActive(user: User): Boolean

    fun getName(): String

}