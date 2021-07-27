package me.func.murder.donate.impl

import me.func.murder.donate.DonatePosition
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.inventory.ItemStack

enum class StepParticle : DonatePosition {
    NONE, ;

    override fun getTitle(): String {
        TODO("Not yet implemented")
    }

    override fun getPrice(): Int {
        TODO("Not yet implemented")
    }

    override fun getRare(): Rare {
        TODO("Not yet implemented")
    }

    override fun getIcon(): ItemStack {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        return name
    }

    override fun give(user: User) {
        TODO("Not yet implemented")
    }

    override fun isActive(user: User): Boolean {
        TODO("Not yet implemented")
    }
}