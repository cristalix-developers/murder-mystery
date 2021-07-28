package me.func.murder.donate.impl

import dev.implario.bukkit.item.item
import me.func.murder.donate.DonatePosition
import me.func.murder.donate.Rare
import me.func.murder.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object LootboxUnit : DonatePosition {
    override fun getTitle(): String {
        return "Лутбокс"
    }

    override fun getPrice(): Int {
        return 10 * 64
    }

    override fun getRare(): Rare {
        return Rare.LEGENDARY
    }

    override fun getIcon(): ItemStack {
        return item {
            type = Material.CLAY_BALL
            nbt("other", "enderchest1")
            text("§bЛутбокс\n\n§7Получить лутбокс,\n§7за §e10 стаков монет§7.")
        }.build()
    }

    override fun give(user: User) {
        user.stat.lootbox++
    }

    override fun isActive(user: User): Boolean {
        return false
    }

    override fun getName(): String {
        return "Lootbox"
    }
}