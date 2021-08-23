package me.func.commons.donate.impl

import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.Rare
import me.func.commons.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object StarterPack : DonatePosition {
    override fun getTitle(): String {
        return "Начальный набор"
    }

    override fun getPrice(): Int {
        return 89
    }

    override fun getRare(): Rare {
        return Rare.LEGENDARY
    }

    override fun getIcon(): ItemStack {
        return item {
            type = Material.CLAY_BALL
            enchant(Enchantment.LUCK, 0)
            nbt("other", "unique")
            nbt("HideFlags", 63)
            text("§bСтартовый набор\n\n§7Вы получите §b3 лутбокса\n§7и §e512 монет§7.\n\n§7Купить за §b89 кристаликов")
        }.build()
    }

    override fun give(user: User) {
        user.stat.lootbox += 3
        user.giveMoney(512)
    }

    override fun isActive(user: User): Boolean {
        return false
    }

    override fun getName(): String {
        return "StarterPack"
    }
}