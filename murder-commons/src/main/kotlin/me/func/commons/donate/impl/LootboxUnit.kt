package me.func.commons.donate.impl

import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.Rare
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object LootboxUnit : DonatePosition {
    override val title: String = "Лутбокс"
    override val name: String = "Lootbox"
    override val price: Int = 10 * 64
    override val rare: Rare = Rare.LEGENDARY

    override val icon: ItemStack = item {
        type = Material.CLAY_BALL
        nbt("other", "enderchest1")
        text("§bЛутбокс\n\n§7Получить лутбокс,\n§7за §e10 стаков монет§7.")
    }.build()

    override fun give(user: User) {
        user.stat.lootbox++
    }

    override fun isActive(user: User) = false
}
